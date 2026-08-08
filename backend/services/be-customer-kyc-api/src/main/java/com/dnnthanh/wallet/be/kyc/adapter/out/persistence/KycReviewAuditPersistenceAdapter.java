package com.dnnthanh.wallet.be.kyc.adapter.out.persistence;

import com.dnnthanh.wallet.be.kyc.adapter.out.persistence.entity.KycReviewAuditEntity;
import com.dnnthanh.wallet.be.kyc.adapter.out.persistence.repository.KycReviewAuditJpaRepository;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycReviewAuditPort;
import com.dnnthanh.wallet.be.kyc.domain.KycReviewDecision;
import com.dnnthanh.wallet.be.platform.stereotype.Persistence;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Persistence
@RequiredArgsConstructor
public class KycReviewAuditPersistenceAdapter implements KycReviewAuditPort {
    private final KycReviewAuditJpaRepository repository;

    @Override
    public void appendReview(
            UUID kycId,
            String reviewerUserId,
            KycReviewDecision decision,
            String rejectionReasonCode,
            Instant reviewedAt) {
        KycReviewAuditEntity entity = new KycReviewAuditEntity();
        entity.setAuditId(UUID.randomUUID());
        entity.setKycId(kycId);
        entity.setReviewerUserId(reviewerUserId);
        entity.setDecision(decision);
        entity.setRejectionReasonCode(rejectionReasonCode);
        entity.setReviewedAt(reviewedAt);
        repository.save(entity);
    }
}
