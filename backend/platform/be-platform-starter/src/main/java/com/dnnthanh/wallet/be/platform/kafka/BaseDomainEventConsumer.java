package com.dnnthanh.wallet.be.platform.kafka;

import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.KAFKA_EVENT_ID_REQUIRED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.KAFKA_EVENT_PAYLOAD_REQUIRED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.KAFKA_EVENT_REQUIRED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.KAFKA_EVENT_TYPE_REQUIRED;

import com.dnnthanh.wallet.be.platform.event.EventEnvelope;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseDomainEventConsumer<T> {
    protected final boolean accepts(EventEnvelope<T> event, String... supportedEventTypes) {
        Objects.requireNonNull(event, KAFKA_EVENT_REQUIRED);
        Objects.requireNonNull(event.eventId(), KAFKA_EVENT_ID_REQUIRED);
        Objects.requireNonNull(event.eventType(), KAFKA_EVENT_TYPE_REQUIRED);
        Set<String> supported =
                Arrays.stream(supportedEventTypes).collect(Collectors.toUnmodifiableSet());
        boolean accepted = supported.contains(event.eventType());
        if (!accepted) {
            log.debug(
                    "kafka_event_ignored consumer={} eventId={} eventType={} supported={}",
                    getClass().getSimpleName(),
                    event.eventId(),
                    event.eventType(),
                    supported);
        }
        return accepted;
    }

    protected final T payload(EventEnvelope<T> event) {
        Objects.requireNonNull(event, KAFKA_EVENT_REQUIRED);
        return Objects.requireNonNull(event.payload(), KAFKA_EVENT_PAYLOAD_REQUIRED);
    }

    protected final void consume(
            EventEnvelope<T> event,
            Consumer<EventEnvelope<T>> handler,
            String... supportedEventTypes) {
        if (!accepts(event, supportedEventTypes)) {
            return;
        }
        try {
            handler.accept(event);
        } catch (RuntimeException failure) {
            log.warn(
                    "kafka_consume_failed consumer={} eventId={} eventType={} failure={}",
                    getClass().getSimpleName(),
                    event.eventId(),
                    event.eventType(),
                    failure.toString());
            throw failure;
        }
    }
}
