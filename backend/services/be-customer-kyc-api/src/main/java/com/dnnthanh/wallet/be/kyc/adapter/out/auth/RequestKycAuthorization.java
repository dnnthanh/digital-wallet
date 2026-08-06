package com.dnnthanh.wallet.be.kyc.adapter.out.auth;

import com.dnnthanh.wallet.be.kyc.application.port.out.KycAuthorizationPort;
import com.dnnthanh.wallet.be.kyc.exception.KycErrorCode;
import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.platform.security.CurrentAuthorization;
import com.dnnthanh.wallet.be.platform.security.WalletAuthorization;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class RequestKycAuthorization implements KycAuthorizationPort {
    private final CurrentAuthorization currentAuthorization;
    private final WalletAuthorization walletAuthorization;

    public RequestKycAuthorization(CurrentAuthorization currentAuthorization) {
        this.currentAuthorization = currentAuthorization;
        this.walletAuthorization = new WalletAuthorization(currentAuthorization);
    }

    @Override
    public String requireCustomerScope() {
        List<String> scopes =
                currentAuthorization.scopes().stream()
                        .filter(StringUtils::isNotBlank)
                        .distinct()
                        .sorted()
                        .toList();
        List<String> leafScopes =
                scopes.stream()
                        .filter(
                                scope ->
                                        scopes.stream()
                                                .noneMatch(
                                                        other ->
                                                                !scope.equals(other)
                                                                        && other.startsWith(
                                                                                scope + "/")))
                        .toList();
        if (leafScopes.size() != 1) {
            throw new BusinessException(KycErrorCode.KYC_SCOPE_REQUIRED);
        }
        return leafScopes.getFirst();
    }

    @Override
    public boolean hasScope(String requiredScope) {
        return walletAuthorization.hasScope(requiredScope);
    }
}
