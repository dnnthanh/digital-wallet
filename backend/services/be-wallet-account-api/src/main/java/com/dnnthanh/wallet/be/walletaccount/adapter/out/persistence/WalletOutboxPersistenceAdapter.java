package com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence;

import com.dnnthanh.wallet.be.platform.outbox.OutboxPayloadCodec;
import com.dnnthanh.wallet.be.platform.stereotype.Persistence;
import com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence.entity.WalletOutboxEntity;
import com.dnnthanh.wallet.be.walletaccount.adapter.out.persistence.repository.WalletOutboxJpaRepository;
import com.dnnthanh.wallet.be.walletaccount.application.event.WalletAccountCreatedPayload;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.WalletOutboxPort;
import com.dnnthanh.wallet.be.walletaccount.constant.WalletAccountEventTypes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Persistence
@RequiredArgsConstructor
public class WalletOutboxPersistenceAdapter implements WalletOutboxPort {
    private final WalletOutboxJpaRepository repository;
    private final OutboxPayloadCodec payloadCodec;

    @Override
    public void appendCreated(WalletAccountCreatedPayload payload) {
        WalletOutboxEntity entity = new WalletOutboxEntity();
        entity.setEventId(UUID.randomUUID());
        entity.setAggregateId(payload.walletId());
        entity.setEventType(WalletAccountEventTypes.WALLET_ACCOUNT_CREATED);
        entity.setPayload(payloadCodec.write(payload));
        entity.setCreatedAt(payload.createdAt());
        entity.setAttemptCount(0);
        repository.save(entity);
    }
}
