package com.dnnthanh.wallet.be.walletaccount.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.walletaccount.exception.WalletAccountErrorCode;
import com.dnnthanh.wallet.be.walletaccount.infrastructure.WalletAccountProperties;
import java.net.URI;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WalletCurrencyPolicyTest {
    private final WalletCurrencyPolicy policy =
            new WalletCurrencyPolicy(
                    new WalletAccountProperties(
                            URI.create("http://localhost:8082"), Set.of("VND", "USD")));

    @Test
    void normalizesWhitespaceAndCaseForSupportedIsoCurrency() {
        assertThat(policy.normalizeSupported(" vnd ")).isEqualTo("VND");
    }

    @Test
    void rejectsCurrencyOutsideConfiguredAllowList() {
        assertThatThrownBy(() -> policy.normalizeSupported("EUR"))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(
                                                WalletAccountErrorCode
                                                        .WALLET_UNSUPPORTED_CURRENCY));
    }

    @Test
    void rejectsMalformedIsoCurrency() {
        assertThatThrownBy(() -> policy.normalizeSupported("NOT-A-CURRENCY"))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(
                                                WalletAccountErrorCode
                                                        .WALLET_UNSUPPORTED_CURRENCY));
    }
}
