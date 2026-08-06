package com.dnnthanh.wallet.be.kyc.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dnnthanh.wallet.be.kyc.exception.KycErrorCode;
import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CustomerKycTest {
    private static final Instant NOW = Instant.parse("2026-08-06T10:00:00Z");
    private static final String USER_ID = "user-123";
    private static final String SCOPE = "/bank/demo-branch";

    @Test
    void createDraftStartsInDraft() {
        CustomerKyc kyc = CustomerKyc.createDraft(UUID.randomUUID(), USER_ID, SCOPE, profile("fp-1"), NOW);

        assertThat(kyc.status()).isEqualTo(KycStatus.DRAFT);
        assertThat(kyc.submittedAt()).isNull();
        assertThat(kyc.reviewedAt()).isNull();
    }

    @Test
    void submitDraftMovesToPendingReview() {
        CustomerKyc pending = draft().submit(NOW.plusSeconds(30));

        assertThat(pending.status()).isEqualTo(KycStatus.PENDING_REVIEW);
        assertThat(pending.submittedAt()).isEqualTo(NOW.plusSeconds(30));
    }

    @Test
    void pendingReviewCannotBeEdited() {
        CustomerKyc pending = draft().submit(NOW.plusSeconds(30));

        assertThatThrownBy(() -> pending.updateDraft(profile("fp-2"), NOW.plusSeconds(60)))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(KycErrorCode.KYC_NOT_EDITABLE));
    }

    @Test
    void verifiedProfileCannotBeEdited() {
        CustomerKyc verified =
                draft()
                        .submit(NOW.plusSeconds(30))
                        .review(KycReviewDecision.VERIFY, "reviewer-1", null, NOW.plusSeconds(60));

        assertThatThrownBy(() -> verified.updateDraft(profile("fp-2"), NOW.plusSeconds(90)))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(KycErrorCode.KYC_NOT_EDITABLE));
    }

    @Test
    void editingRejectedProfileReturnsToDraftAndClearsReviewMetadata() {
        CustomerKyc rejected =
                draft()
                        .submit(NOW.plusSeconds(30))
                        .review(KycReviewDecision.REJECT, "reviewer-1", "DOC_UNCLEAR", NOW.plusSeconds(60));

        CustomerKyc updated = rejected.updateDraft(profile("fp-2"), NOW.plusSeconds(90));

        assertThat(updated.status()).isEqualTo(KycStatus.DRAFT);
        assertThat(updated.reviewedAt()).isNull();
        assertThat(updated.reviewedBy()).isNull();
        assertThat(updated.rejectionReasonCode()).isNull();
        assertThat(updated.submittedAt()).isNull();
    }

    @Test
    void onlyPendingReviewCanBeReviewed() {
        assertThatThrownBy(() -> draft().review(KycReviewDecision.VERIFY, "reviewer-1", null, NOW))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(KycErrorCode.KYC_NOT_REVIEWABLE));
    }

    @Test
    void rejectionRequiresStableReasonCode() {
        CustomerKyc pending = draft().submit(NOW.plusSeconds(30));

        assertThatThrownBy(() -> pending.review(KycReviewDecision.REJECT, "reviewer-1", "  ", NOW.plusSeconds(60)))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(KycErrorCode.KYC_REJECTION_REASON_REQUIRED));
    }

    @Test
    void reviewerCannotReviewOwnProfile() {
        CustomerKyc pending = draft().submit(NOW.plusSeconds(30));

        assertThatThrownBy(() -> pending.review(KycReviewDecision.VERIFY, USER_ID, null, NOW.plusSeconds(60)))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(KycErrorCode.KYC_SELF_REVIEW_FORBIDDEN));
    }

    private static CustomerKyc draft() {
        return CustomerKyc.createDraft(UUID.randomUUID(), USER_ID, SCOPE, profile("fp-1"), NOW);
    }

    private static KycProfile profile(String fingerprint) {
        return new KycProfile(
                "Nguyen Van A",
                LocalDate.of(1998, 4, 12),
                "VN",
                KycDocumentType.NATIONAL_ID,
                fingerprint,
                "1234",
                "VN",
                LocalDate.of(2032, 4, 12),
                "1 Nguyen Hue",
                "Ho Chi Minh City",
                "VN");
    }
}
