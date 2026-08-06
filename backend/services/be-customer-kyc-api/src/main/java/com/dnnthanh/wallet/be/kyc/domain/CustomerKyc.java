package com.dnnthanh.wallet.be.kyc.domain;

import static com.dnnthanh.wallet.be.kyc.constant.KycInvariantMessages.REVIEW_DECISION_REQUIRED;

import com.dnnthanh.wallet.be.kyc.exception.KycErrorCode;
import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public record CustomerKyc(
        UUID kycId,
        String userId,
        String scopePath,
        KycProfile profile,
        KycStatus status,
        Instant submittedAt,
        Instant reviewedAt,
        String reviewedBy,
        String rejectionReasonCode,
        Instant createdAt,
        Instant updatedAt,
        long version) {

    public static CustomerKyc createDraft(
            UUID kycId, String userId, String scopePath, KycProfile profile, Instant now) {
        return new CustomerKyc(
                kycId,
                userId,
                scopePath,
                profile,
                KycStatus.DRAFT,
                null,
                null,
                null,
                null,
                now,
                now,
                0L);
    }

    public CustomerKyc updateDraft(KycProfile updatedProfile, Instant now) {
        if (status == KycStatus.PENDING_REVIEW || status == KycStatus.VERIFIED) {
            throw new BusinessException(KycErrorCode.KYC_NOT_EDITABLE);
        }
        return new CustomerKyc(
                kycId,
                userId,
                scopePath,
                updatedProfile,
                KycStatus.DRAFT,
                null,
                null,
                null,
                null,
                createdAt,
                now,
                version);
    }

    public CustomerKyc submit(Instant now) {
        if (status != KycStatus.DRAFT) {
            throw new BusinessException(KycErrorCode.KYC_NOT_SUBMITTABLE);
        }
        return new CustomerKyc(
                kycId,
                userId,
                scopePath,
                profile,
                KycStatus.PENDING_REVIEW,
                now,
                null,
                null,
                null,
                createdAt,
                now,
                version);
    }

    public CustomerKyc review(
            KycReviewDecision decision,
            String reviewerUserId,
            String rejectionReasonCode,
            Instant now) {
        if (status != KycStatus.PENDING_REVIEW) {
            throw new BusinessException(KycErrorCode.KYC_NOT_REVIEWABLE);
        }
        if (Objects.equals(userId, reviewerUserId)) {
            throw new BusinessException(KycErrorCode.KYC_SELF_REVIEW_FORBIDDEN);
        }
        KycReviewDecision requiredDecision =
                Objects.requireNonNull(decision, REVIEW_DECISION_REQUIRED);
        if (requiredDecision == KycReviewDecision.REJECT
                && StringUtils.isBlank(rejectionReasonCode)) {
            throw new BusinessException(KycErrorCode.KYC_REJECTION_REASON_REQUIRED);
        }

        KycStatus reviewedStatus =
                requiredDecision == KycReviewDecision.VERIFY
                        ? KycStatus.VERIFIED
                        : KycStatus.REJECTED;
        String reviewedRejectionReason =
                requiredDecision == KycReviewDecision.REJECT ? rejectionReasonCode : null;
        return new CustomerKyc(
                kycId,
                userId,
                scopePath,
                profile,
                reviewedStatus,
                submittedAt,
                now,
                reviewerUserId,
                reviewedRejectionReason,
                createdAt,
                now,
                version);
    }
}
