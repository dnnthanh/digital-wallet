package com.dnnthanh.wallet.be.walletaccount.api.response;

import com.dnnthanh.wallet.be.walletaccount.domain.WalletStatus;
import java.time.Instant;
import java.util.UUID;

public record WalletAccountResponse(
        UUID walletId,
        String userId,
        String currency,
        WalletStatus status,
        Instant createdAt,
        Instant updatedAt) {}
