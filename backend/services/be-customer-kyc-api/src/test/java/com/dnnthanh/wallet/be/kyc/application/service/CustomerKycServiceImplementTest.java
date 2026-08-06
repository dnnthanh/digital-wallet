package com.dnnthanh.wallet.be.kyc.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnnthanh.wallet.be.kyc.application.event.KycStatusChangedPayload;
import com.dnnthanh.wallet.be.kyc.application.model.DocumentFingerprint;
import com.dnnthanh.wallet.be.kyc.application.model.KycReviewCommand;
import com.dnnthanh.wallet.be.kyc.application.model.KycView;
import com.dnnthanh.wallet.be.kyc.application.model.UpsertKycDraftCommand;
import com.dnnthanh.wallet.be.kyc.application.port.out.CurrentActorPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.CustomerKycRepositoryPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.DocumentFingerprintPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycAuthorizationPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycOutboxPort;
import com.dnnthanh.wallet.be.kyc.domain.CustomerKyc;
import com.dnnthanh.wallet.be.kyc.domain.KycDocumentType;
import com.dnnthanh.wallet.be.kyc.domain.KycProfile;
import com.dnnthanh.wallet.be.kyc.domain.KycReviewDecision;
import com.dnnthanh.wallet.be.kyc.domain.KycStatus;
import com.dnnthanh.wallet.be.kyc.exception.KycErrorCode;
import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerKycServiceImplementTest {
    private static final Instant NOW = Instant.parse("2026-08-06T10:00:00Z");
    private static final String USER_ID = "user-123";
    private static final String REVIEWER_ID = "reviewer-123";
    private static final String SCOPE = "/bank/demo-branch";

    @Mock private CustomerKycRepositoryPort repository;
    @Mock private DocumentFingerprintPort fingerprintPort;
    @Mock private KycOutboxPort outboxPort;
    @Mock private CurrentActorPort currentActorPort;
    @Mock private KycAuthorizationPort authorizationPort;

    private CustomerKycServiceImplement service;

    @BeforeEach
    void setUp() {
        service =
                new CustomerKycServiceImplement(
                        repository,
                        fingerprintPort,
                        outboxPort,
                        currentActorPort,
                        authorizationPort,
                        Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsOwnedDraftWithFingerprintAndSafeView() {
        when(currentActorPort.userId()).thenReturn(USER_ID);
        when(authorizationPort.requireCustomerScope()).thenReturn(SCOPE);
        when(fingerprintPort.fingerprint("123456789012"))
                .thenReturn(new DocumentFingerprint("fingerprint-1", "9012"));
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(repository.save(any(CustomerKyc.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KycView result = service.upsertMyDraft(command("123456789012"));

        ArgumentCaptor<CustomerKyc> saved = ArgumentCaptor.forClass(CustomerKyc.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().userId()).isEqualTo(USER_ID);
        assertThat(saved.getValue().scopePath()).isEqualTo(SCOPE);
        assertThat(saved.getValue().profile().documentFingerprint()).isEqualTo("fingerprint-1");
        assertThat(result.documentLast4()).isEqualTo("9012");
        assertThat(result.status()).isEqualTo(KycStatus.DRAFT);
        verify(outboxPort, never()).appendStatusChanged(any());
    }

    @Test
    void editingRejectedDraftEmitsRejectedToDraftEvent() {
        CustomerKyc rejected =
                draft(USER_ID, SCOPE)
                        .submit(NOW.minusSeconds(90))
                        .review(
                                KycReviewDecision.REJECT,
                                REVIEWER_ID,
                                "DOC_UNCLEAR",
                                NOW.minusSeconds(60));
        when(currentActorPort.userId()).thenReturn(USER_ID);
        when(authorizationPort.requireCustomerScope()).thenReturn(SCOPE);
        when(fingerprintPort.fingerprint("123456789012"))
                .thenReturn(new DocumentFingerprint("fingerprint-2", "9012"));
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(rejected));
        when(repository.save(any(CustomerKyc.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KycView result = service.upsertMyDraft(command("123456789012"));

        ArgumentCaptor<KycStatusChangedPayload> event =
                ArgumentCaptor.forClass(KycStatusChangedPayload.class);
        verify(outboxPort).appendStatusChanged(event.capture());
        assertThat(result.status()).isEqualTo(KycStatus.DRAFT);
        assertThat(event.getValue().previousStatus()).isEqualTo(KycStatus.REJECTED);
        assertThat(event.getValue().currentStatus()).isEqualTo(KycStatus.DRAFT);
    }

    @Test
    void submitPersistsTransitionAndAppendsMinimalStatusEvent() {
        CustomerKyc draft = draft(USER_ID, SCOPE);
        when(currentActorPort.userId()).thenReturn(USER_ID);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(draft));
        when(repository.save(any(CustomerKyc.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KycView result = service.submitMyKyc();

        ArgumentCaptor<KycStatusChangedPayload> event =
                ArgumentCaptor.forClass(KycStatusChangedPayload.class);
        verify(outboxPort).appendStatusChanged(event.capture());
        assertThat(result.status()).isEqualTo(KycStatus.PENDING_REVIEW);
        assertThat(event.getValue().kycId()).isEqualTo(draft.kycId());
        assertThat(event.getValue().userId()).isEqualTo(USER_ID);
        assertThat(event.getValue().previousStatus()).isEqualTo(KycStatus.DRAFT);
        assertThat(event.getValue().currentStatus()).isEqualTo(KycStatus.PENDING_REVIEW);
        assertThat(event.getValue().changedAt()).isEqualTo(NOW);
    }

    @Test
    void reviewerWithoutHierarchicalScopeCannotReadTarget() {
        CustomerKyc pending = draft(USER_ID, SCOPE).submit(NOW.minusSeconds(60));
        when(currentActorPort.userId()).thenReturn(REVIEWER_ID);
        when(repository.findById(pending.kycId())).thenReturn(Optional.of(pending));
        when(authorizationPort.hasScope(SCOPE)).thenReturn(false);

        assertThatThrownBy(() -> service.getReview(pending.kycId()))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(KycErrorCode.KYC_SCOPE_REQUIRED));
    }

    @Test
    void scopedReviewerCanRejectAndEmitStatusOnlyEvent() {
        CustomerKyc pending = draft(USER_ID, SCOPE).submit(NOW.minusSeconds(60));
        when(currentActorPort.userId()).thenReturn(REVIEWER_ID);
        when(repository.findById(pending.kycId())).thenReturn(Optional.of(pending));
        when(authorizationPort.hasScope(SCOPE)).thenReturn(true);
        when(repository.save(any(CustomerKyc.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KycView result =
                service.review(
                        pending.kycId(),
                        new KycReviewCommand(KycReviewDecision.REJECT, "DOC_UNCLEAR"));

        ArgumentCaptor<KycStatusChangedPayload> event =
                ArgumentCaptor.forClass(KycStatusChangedPayload.class);
        verify(outboxPort).appendStatusChanged(event.capture());
        assertThat(result.status()).isEqualTo(KycStatus.REJECTED);
        assertThat(result.rejectionReasonCode()).isEqualTo("DOC_UNCLEAR");
        assertThat(event.getValue().previousStatus()).isEqualTo(KycStatus.PENDING_REVIEW);
        assertThat(event.getValue().currentStatus()).isEqualTo(KycStatus.REJECTED);
    }

    private static UpsertKycDraftCommand command(String rawDocumentNumber) {
        return new UpsertKycDraftCommand(
                "Nguyen Van A",
                LocalDate.of(1998, 4, 12),
                "VN",
                KycDocumentType.NATIONAL_ID,
                rawDocumentNumber,
                "VN",
                LocalDate.of(2032, 4, 12),
                "1 Nguyen Hue",
                "Ho Chi Minh City",
                "VN");
    }

    private static CustomerKyc draft(String userId, String scope) {
        return CustomerKyc.createDraft(
                UUID.randomUUID(),
                userId,
                scope,
                new KycProfile(
                        "Nguyen Van A",
                        LocalDate.of(1998, 4, 12),
                        "VN",
                        KycDocumentType.NATIONAL_ID,
                        "fingerprint-1",
                        "9012",
                        "VN",
                        LocalDate.of(2032, 4, 12),
                        "1 Nguyen Hue",
                        "Ho Chi Minh City",
                        "VN"),
                NOW.minusSeconds(120));
    }
}
