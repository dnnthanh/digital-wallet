package com.dnnthanh.wallet.be.platform.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        int eventVersion,
        String aggregateId,
        LocalDateTime occurredAt,
        String traceId,
        String correlationId,
        String causationId,
        T payload) {
    public static <T> EventEnvelope<T> create(
            String eventType, String aggregateId, String traceId, T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                1,
                aggregateId,
                LocalDateTime.now(),
                traceId,
                traceId,
                null,
                payload);
    }
}
