package com.dnnthanh.wallet.be.kyc.application.port.out;

public interface KycAuthorizationPort {
    String requireCustomerScope();

    boolean hasScope(String requiredScope);
}
