package com.dnnthanh.wallet.be.walletaccount.application.service;

import com.dnnthanh.wallet.be.walletaccount.application.event.WalletAccountCreatedPayload;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.WalletAccountRepositoryPort;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.WalletOutboxPort;
import com.dnnthanh.wallet.be.walletaccount.domain.WalletAccount;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletAccountWriteTransactionImplement implements WalletAccountWriteTransaction {
    private final WalletAccountRepositoryPort repository;
    private final WalletOutboxPort outboxPort;
    private final Clock clock;

    @Override
    @Transactional
    public WalletAccount open(String userId, String scopePath, String currency) {
        Instant now = clock.instant();
        WalletAccount saved =
                repository.save(WalletAccount.create(UUID.randomUUID(), userId, scopePath, currency, now));
        outboxPort.appendCreated(
                new WalletAccountCreatedPayload(
                        saved.walletId(),
                        saved.userId(),
                        saved.currency(),
                        saved.status().name(),
                        saved.createdAt()));
        return saved;
    }
}
