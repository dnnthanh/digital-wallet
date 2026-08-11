package com.dnnthanh.wallet.be.walletaccount.domain;

import java.time.Instant;
import java.util.UUID;

public record WalletAccount(
        UUID walletId,
        String userId,
        String scopePath,
        String currency,
        WalletStatus status,
        Instant createdAt,
        Instant updatedAt,
        long version) {

    public static WalletAccount create(
            UUID walletId, String userId, String scopePath, String currency, Instant now) {
        return new WalletAccount(
                walletId, userId, scopePath, currency, WalletStatus.ACTIVE, now, now, 0L);
    }
}
