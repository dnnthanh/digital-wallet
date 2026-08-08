package com.dnnthanh.wallet.be.kyc.adapter.out.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dnnthanh.wallet.be.kyc.exception.KycErrorCode;
import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.platform.security.CurrentAuthorization;
import com.dnnthanh.wallet.be.platform.security.WalletAuthorization;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RequestKycAuthorizationTest {
    @Test
    void choosesMostSpecificCustomerScopeAndUsesHierarchicalChecks() {
        RequestKycAuthorization authorization =
                authorization(
                        new CurrentAuthorization(
                                Set.of("kyc:review"), Set.of("/bank", "/bank/demo-branch")));

        assertThat(authorization.requireCustomerScope()).isEqualTo("/bank/demo-branch");
        assertThat(authorization.hasScope("/bank/demo-branch/customer-1")).isTrue();
        assertThat(authorization.hasScope("/other/customer-1")).isFalse();
    }

    @Test
    void missingScopeFailsClosed() {
        RequestKycAuthorization authorization =
                authorization(new CurrentAuthorization(Set.of("kyc:self:write"), Set.of()));

        assertThatThrownBy(authorization::requireCustomerScope)
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(KycErrorCode.KYC_SCOPE_REQUIRED));
    }

    @Test
    void unrelatedLeafScopesFailClosedInsteadOfChoosingArbitrarily() {
        RequestKycAuthorization authorization =
                authorization(
                        new CurrentAuthorization(
                                Set.of("kyc:self:write"), Set.of("/bank/a", "/bank/b")));

        assertThatThrownBy(authorization::requireCustomerScope)
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(KycErrorCode.KYC_SCOPE_REQUIRED));
    }

    private static RequestKycAuthorization authorization(
            CurrentAuthorization currentAuthorization) {
        return new RequestKycAuthorization(
                currentAuthorization, new WalletAuthorization(currentAuthorization));
    }
}
