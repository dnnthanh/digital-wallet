package com.dnnthanh.wallet.be.platform.event;

import com.dnnthanh.wallet.be.platform.messaging.OutboxMessage;
import com.dnnthanh.wallet.be.platform.outbox.OutboxPayloadCodec;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventFactory {
    private final OutboxPayloadCodec payloadCodec;

    public <T> EventEnvelope<T> fromOutbox(OutboxMessage message, Class<T> payloadType) {
        T payload = payloadCodec.read(message.payload(), payloadType);
        LocalDateTime occurredAt = LocalDateTime.ofInstant(message.occurredAt(), ZoneOffset.UTC);
        return new EventEnvelope<>(
                message.id(),
                message.eventType(),
                1,
                message.aggregateId(),
                occurredAt,
                null,
                message.correlationId(),
                null,
                payload);
    }
}
