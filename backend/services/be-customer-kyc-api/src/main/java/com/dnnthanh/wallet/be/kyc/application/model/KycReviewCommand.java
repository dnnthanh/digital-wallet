package com.dnnthanh.wallet.be.kyc.application.model;

import com.dnnthanh.wallet.be.kyc.domain.KycReviewDecision;

public record KycReviewCommand(KycReviewDecision decision, String rejectionReasonCode) {}
