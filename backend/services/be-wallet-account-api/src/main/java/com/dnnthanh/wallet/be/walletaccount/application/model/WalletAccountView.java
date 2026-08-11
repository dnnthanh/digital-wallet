package com.dnnthanh.wallet.be.walletaccount.application.model;

import com.dnnthanh.wallet.be.walletaccount.domain.WalletAccount;
import com.dnnthanh.wallet.be.walletaccount.domain.WalletStatus;
import java.time.Instant;
import java.util.UUID;

public record WalletAccountView(
        UUID walletId,
        String userId,
        String scopePath,
        String currency,
        WalletStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static WalletAccountView from(WalletAccount wallet) {
        return new WalletAccountView(
                wallet.walletId(),
                wallet.userId(),
                wallet.scopePath(),
                wallet.currency(),
                wallet.status(),
                wallet.createdAt(),
                wallet.updatedAt());
    }
}
