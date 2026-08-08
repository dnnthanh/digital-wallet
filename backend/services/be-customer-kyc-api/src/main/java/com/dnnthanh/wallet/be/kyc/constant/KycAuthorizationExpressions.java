package com.dnnthanh.wallet.be.kyc.constant;

public final class KycAuthorizationExpressions {
    public static final String KYC_SELF_READ =
            "@walletAuthorization.hasPermission('kyc:self:read')";
    public static final String KYC_SELF_WRITE =
            "@walletAuthorization.hasPermission('kyc:self:write')";
    public static final String KYC_SELF_SUBMIT =
            "@walletAuthorization.hasPermission('kyc:self:submit')";
    public static final String KYC_REVIEW = "@walletAuthorization.hasPermission('kyc:review')";

    private KycAuthorizationExpressions() {}
}
