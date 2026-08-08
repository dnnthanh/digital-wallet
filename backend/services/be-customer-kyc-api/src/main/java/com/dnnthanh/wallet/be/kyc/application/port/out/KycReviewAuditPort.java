package com.dnnthanh.wallet.be.kyc.application.port.out;

import com.dnnthanh.wallet.be.kyc.domain.KycReviewDecision;
import java.time.Instant;
import java.util.UUID;

public interface KycReviewAuditPort {
    void appendReview(
            UUID kycId,
            String reviewerUserId,
            KycReviewDecision decision,
            String rejectionReasonCode,
            Instant reviewedAt);
}
