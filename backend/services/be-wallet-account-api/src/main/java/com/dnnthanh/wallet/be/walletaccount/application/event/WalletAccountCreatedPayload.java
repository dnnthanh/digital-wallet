package com.dnnthanh.wallet.be.walletaccount.application.event;

import java.time.Instant;
import java.util.UUID;

public record WalletAccountCreatedPayload(
        UUID walletId, String userId, String currency, String status, Instant createdAt) {}
