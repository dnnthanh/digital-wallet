package com.dnnthanh.wallet.be.kyc.api.request;

import com.dnnthanh.wallet.be.kyc.application.model.KycReviewCommand;
import com.dnnthanh.wallet.be.kyc.domain.KycReviewDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record KycReviewDecisionRequest(
        @NotNull KycReviewDecision decision,
        @Size(max = 128) String rejectionReasonCode) {
    public KycReviewCommand toCommand() {
        return new KycReviewCommand(decision, rejectionReasonCode);
    }
}
