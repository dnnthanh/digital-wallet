package com.dnnthanh.wallet.be.kyc.adapter.out.persistence;

import static com.dnnthanh.wallet.be.kyc.constant.KycEventTypes.CUSTOMER_KYC_STATUS_CHANGED;

import com.dnnthanh.wallet.be.kyc.adapter.out.persistence.entity.KycOutboxEntity;
import com.dnnthanh.wallet.be.kyc.adapter.out.persistence.repository.KycOutboxJpaRepository;
import com.dnnthanh.wallet.be.kyc.application.event.KycStatusChangedPayload;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycOutboxPort;
import com.dnnthanh.wallet.be.platform.outbox.OutboxPayloadCodec;
import com.dnnthanh.wallet.be.platform.stereotype.Persistence;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Persistence
@RequiredArgsConstructor
public class KycOutboxPersistenceAdapter implements KycOutboxPort {
    private final KycOutboxJpaRepository repository;
    private final OutboxPayloadCodec payloadCodec;

    @Override
    public void appendStatusChanged(KycStatusChangedPayload payload) {
        KycOutboxEntity entity = new KycOutboxEntity();
        entity.setEventId(UUID.randomUUID());
        entity.setAggregateId(payload.kycId());
        entity.setEventType(CUSTOMER_KYC_STATUS_CHANGED);
        entity.setPayload(payloadCodec.write(payload));
        entity.setCreatedAt(payload.changedAt());
        entity.setAttemptCount(0);
        repository.save(entity);
    }
}
