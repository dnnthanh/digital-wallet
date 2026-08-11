package com.dnnthanh.wallet.be.walletaccount.application.service;

import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.walletaccount.exception.WalletAccountErrorCode;
import com.dnnthanh.wallet.be.walletaccount.infrastructure.WalletAccountProperties;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletCurrencyPolicy {
    private final WalletAccountProperties properties;

    public String normalizeSupported(String rawCurrency) {
        String trimmed = StringUtils.trimToNull(rawCurrency);
        if (Objects.isNull(trimmed)) {
            throw unsupportedCurrency();
        }
        String normalized = trimmed.toUpperCase(Locale.ROOT);
        try {
            Currency.getInstance(normalized);
        } catch (IllegalArgumentException exception) {
            throw unsupportedCurrency();
        }
        Set<String> supported =
                Objects.requireNonNullElse(properties.supportedCurrencies(), Set.<String>of())
                        .stream()
                        .map(StringUtils::trimToEmpty)
                        .map(value -> value.toUpperCase(Locale.ROOT))
                        .collect(Collectors.toUnmodifiableSet());
        if (!supported.contains(normalized)) {
            throw unsupportedCurrency();
        }
        return normalized;
    }

    private static BusinessException unsupportedCurrency() {
        return new BusinessException(WalletAccountErrorCode.WALLET_UNSUPPORTED_CURRENCY);
    }
}
