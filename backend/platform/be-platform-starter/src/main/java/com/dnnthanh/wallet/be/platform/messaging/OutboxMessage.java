package com.dnnthanh.wallet.be.platform.messaging;

import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_AGGREGATE_ID_REQUIRED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_AGGREGATE_TYPE_REQUIRED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_EVENT_TYPE_REQUIRED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_ID_REQUIRED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_OCCURRED_AT_REQUIRED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_PAYLOAD_REQUIRED;

import java.time.Instant;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

public record OutboxMessage(
        UUID id,
        String eventType,
        String aggregateType,
        String aggregateId,
        String payload,
        Instant occurredAt,
        String correlationId) {
    public OutboxMessage {
        Validate.notNull(id, OUTBOX_ID_REQUIRED);
        Validate.notBlank(eventType, OUTBOX_EVENT_TYPE_REQUIRED);
        Validate.notBlank(aggregateType, OUTBOX_AGGREGATE_TYPE_REQUIRED);
        Validate.notBlank(aggregateId, OUTBOX_AGGREGATE_ID_REQUIRED);
        Validate.notNull(payload, OUTBOX_PAYLOAD_REQUIRED);
        Validate.notNull(occurredAt, OUTBOX_OCCURRED_AT_REQUIRED);
    }
}
