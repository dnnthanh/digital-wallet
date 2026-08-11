package com.dnnthanh.wallet.be.walletaccount.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnnthanh.wallet.be.walletaccount.application.event.WalletAccountCreatedPayload;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.WalletAccountRepositoryPort;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.WalletOutboxPort;
import com.dnnthanh.wallet.be.walletaccount.domain.WalletAccount;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletAccountWriteTransactionImplementTest {
    private static final Instant NOW = Instant.parse("2026-08-11T08:00:00Z");

    @Mock private WalletAccountRepositoryPort repository;
    @Mock private WalletOutboxPort outboxPort;

    private WalletAccountWriteTransactionImplement writer;

    @BeforeEach
    void setUp() {
        writer =
                new WalletAccountWriteTransactionImplement(
                        repository, outboxPort, Clock.fixed(NOW, ZoneOffset.UTC));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void savesWalletBeforeAppendingMinimalCreatedEvent() {
        WalletAccount wallet = writer.open("user-1", "/bank/demo-branch", "VND");

        InOrder order = inOrder(repository, outboxPort);
        order.verify(repository).save(any(WalletAccount.class));
        order.verify(outboxPort).appendCreated(any(WalletAccountCreatedPayload.class));

        ArgumentCaptor<WalletAccountCreatedPayload> payloadCaptor =
                ArgumentCaptor.forClass(WalletAccountCreatedPayload.class);
        verify(outboxPort).appendCreated(payloadCaptor.capture());
        WalletAccountCreatedPayload payload = payloadCaptor.getValue();

        assertThat(payload.walletId()).isEqualTo(wallet.walletId());
        assertThat(payload.userId()).isEqualTo("user-1");
        assertThat(payload.currency()).isEqualTo("VND");
        assertThat(payload.status()).isEqualTo("ACTIVE");
        assertThat(payload.createdAt()).isEqualTo(NOW);
        assertThat(payload.getClass().getRecordComponents())
                .extracting(component -> component.getName())
                .doesNotContain("scopePath", "balance", "permissions", "scopes");
    }
}
