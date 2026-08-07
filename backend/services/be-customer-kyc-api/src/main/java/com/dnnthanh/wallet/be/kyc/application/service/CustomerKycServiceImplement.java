package com.dnnthanh.wallet.be.kyc.application.service;

import com.dnnthanh.wallet.be.kyc.application.event.KycStatusChangedPayload;
import com.dnnthanh.wallet.be.kyc.application.model.DocumentFingerprint;
import com.dnnthanh.wallet.be.kyc.application.model.KycReviewCommand;
import com.dnnthanh.wallet.be.kyc.application.model.KycView;
import com.dnnthanh.wallet.be.kyc.application.model.UpsertKycDraftCommand;
import com.dnnthanh.wallet.be.kyc.application.port.in.GetKycReviewQuery;
import com.dnnthanh.wallet.be.kyc.application.port.in.GetMyKycQuery;
import com.dnnthanh.wallet.be.kyc.application.port.in.ReviewKycUseCase;
import com.dnnthanh.wallet.be.kyc.application.port.in.SubmitMyKycUseCase;
import com.dnnthanh.wallet.be.kyc.application.port.in.UpsertMyKycDraftUseCase;
import com.dnnthanh.wallet.be.kyc.application.port.out.CurrentActorPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.CustomerKycRepositoryPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.DocumentFingerprintPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycAuthorizationPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycOutboxPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycReviewAuditPort;
import com.dnnthanh.wallet.be.kyc.domain.CustomerKyc;
import com.dnnthanh.wallet.be.kyc.domain.KycProfile;
import com.dnnthanh.wallet.be.kyc.domain.KycStatus;
import com.dnnthanh.wallet.be.kyc.exception.KycErrorCode;
import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerKycServiceImplement
        implements GetMyKycQuery,
                UpsertMyKycDraftUseCase,
                SubmitMyKycUseCase,
                GetKycReviewQuery,
                ReviewKycUseCase {
    private final CustomerKycRepositoryPort repository;
    private final DocumentFingerprintPort fingerprintPort;
    private final KycOutboxPort outboxPort;
    private final KycReviewAuditPort reviewAuditPort;
    private final CurrentActorPort currentActorPort;
    private final KycAuthorizationPort authorizationPort;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public KycView getMyKyc() {
        return KycView.from(findOwnedKyc());
    }

    @Override
    @Transactional
    public KycView upsertMyDraft(UpsertKycDraftCommand command) {
        String userId = currentActorPort.userId();
        String scopePath = authorizationPort.requireCustomerScope();
        DocumentFingerprint fingerprint = fingerprintPort.fingerprint(command.documentNumber());
        KycProfile profile = toProfile(command, fingerprint);
        Instant now = clock.instant();

        CustomerKyc kyc =
                repository
                        .findByUserId(userId)
                        .map(existing -> updateExistingDraft(existing, profile, now))
                        .orElseGet(
                                () ->
                                        CustomerKyc.createDraft(
                                                UUID.randomUUID(),
                                                userId,
                                                scopePath,
                                                profile,
                                                now));
        return KycView.from(repository.save(kyc));
    }

    @Override
    @Transactional
    public KycView submitMyKyc() {
        CustomerKyc current = findOwnedKyc();
        Instant now = clock.instant();
        CustomerKyc submitted = current.submit(now);
        CustomerKyc saved = repository.save(submitted);
        appendStatusChange(current.status(), saved, now);
        return KycView.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public KycView getReview(UUID kycId) {
        CustomerKyc kyc = findReviewTarget(kycId);
        requireTargetScope(kyc);
        return KycView.from(kyc);
    }

    @Override
    @Transactional
    public KycView review(UUID kycId, KycReviewCommand command) {
        CustomerKyc current = findReviewTarget(kycId);
        requireTargetScope(current);
        Instant now = clock.instant();
        String reviewerUserId = currentActorPort.userId();
        CustomerKyc reviewed =
                current.review(
                        command.decision(), reviewerUserId, command.rejectionReasonCode(), now);
        CustomerKyc saved = repository.save(reviewed);
        reviewAuditPort.appendReview(
                saved.kycId(),
                reviewerUserId,
                command.decision(),
                saved.rejectionReasonCode(),
                now);
        appendStatusChange(current.status(), saved, now);
        return KycView.from(saved);
    }

    private CustomerKyc updateExistingDraft(CustomerKyc existing, KycProfile profile, Instant now) {
        KycStatus previousStatus = existing.status();
        CustomerKyc updated = existing.updateDraft(profile, now);
        if (previousStatus != updated.status()) {
            appendStatusChange(previousStatus, updated, now);
        }
        return updated;
    }

    private CustomerKyc findOwnedKyc() {
        return repository
                .findByUserId(currentActorPort.userId())
                .orElseThrow(() -> new BusinessException(KycErrorCode.KYC_NOT_FOUND));
    }

    private CustomerKyc findReviewTarget(UUID kycId) {
        return repository
                .findById(kycId)
                .orElseThrow(() -> new BusinessException(KycErrorCode.KYC_NOT_FOUND));
    }

    private void requireTargetScope(CustomerKyc kyc) {
        if (!authorizationPort.hasScope(kyc.scopePath())) {
            throw new BusinessException(KycErrorCode.KYC_SCOPE_REQUIRED);
        }
    }

    private void appendStatusChange(
            KycStatus previousStatus, CustomerKyc current, Instant changedAt) {
        outboxPort.appendStatusChanged(
                new KycStatusChangedPayload(
                        current.kycId(),
                        current.userId(),
                        previousStatus,
                        current.status(),
                        changedAt));
    }

    private KycProfile toProfile(UpsertKycDraftCommand command, DocumentFingerprint fingerprint) {
        return new KycProfile(
                command.legalName(),
                command.dateOfBirth(),
                command.nationality(),
                command.documentType(),
                fingerprint.fingerprint(),
                fingerprint.last4(),
                command.documentCountry(),
                command.documentExpiresAt(),
                command.addressLine1(),
                command.city(),
                command.country());
    }
}
