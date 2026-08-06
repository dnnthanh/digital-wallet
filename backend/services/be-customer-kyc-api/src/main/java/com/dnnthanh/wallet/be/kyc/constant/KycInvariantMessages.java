package com.dnnthanh.wallet.be.kyc.constant;

public final class KycInvariantMessages {
    public static final String DOCUMENT_HMAC_SECRET_REQUIRED =
            "KYC document HMAC secret must be configured";
    public static final String DOCUMENT_NUMBER_REQUIRED = "KYC document number is required";
    public static final String DOCUMENT_NUMBER_INVALID = "KYC document number is invalid";
    public static final String DOCUMENT_FINGERPRINT_FAILED = "KYC document fingerprinting failed";
    public static final String REVIEW_DECISION_REQUIRED = "KYC review decision is required";

    private KycInvariantMessages() {}
}
