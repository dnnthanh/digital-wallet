package com.dnnthanh.wallet.be.walletaccount.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WalletAccountTest {
    private static final Instant NOW = Instant.parse("2026-08-11T07:30:00Z");

    @Test
    void createsActiveWalletWithImmutableIdentityFieldsAndNoBalanceContract() {
        UUID walletId = UUID.fromString("5f0244d1-6be6-4c90-9037-d29831d31462");

        WalletAccount wallet =
                WalletAccount.create(walletId, "user-1", "/bank/demo-branch", "VND", NOW);

        assertThat(wallet.walletId()).isEqualTo(walletId);
        assertThat(wallet.userId()).isEqualTo("user-1");
        assertThat(wallet.scopePath()).isEqualTo("/bank/demo-branch");
        assertThat(wallet.currency()).isEqualTo("VND");
        assertThat(wallet.status()).isEqualTo(WalletStatus.ACTIVE);
        assertThat(wallet.createdAt()).isEqualTo(NOW);
        assertThat(wallet.updatedAt()).isEqualTo(NOW);
        assertThat(
                        Arrays.stream(WalletAccount.class.getDeclaredMethods())
                                .map(method -> method.getName().toLowerCase())
                                .noneMatch(name -> name.contains("balance")))
                .isTrue();
    }
}
