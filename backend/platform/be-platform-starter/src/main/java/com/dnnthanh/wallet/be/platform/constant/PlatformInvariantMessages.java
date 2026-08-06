package com.dnnthanh.wallet.be.platform.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PlatformInvariantMessages {
    public static final String ERROR_CODE_REQUIRED = "errorCode is required";
    public static final String GRPC_DEFAULT_DEADLINE_REQUIRED = "defaultDeadline is required";
    public static final String GRPC_DEFAULT_DEADLINE_POSITIVE = "defaultDeadline must be positive";
    public static final String KAFKA_TEMPLATE_REQUIRED = "kafkaTemplate is required";
    public static final String KAFKA_TOPIC_REQUIRED = "topic is required";
    public static final String KAFKA_VALUE_REQUIRED = "value is required";
    public static final String KAFKA_EVENT_REQUIRED = "event is required";
    public static final String KAFKA_EVENT_ID_REQUIRED = "event.eventId is required";
    public static final String KAFKA_EVENT_TYPE_REQUIRED = "event.eventType is required";
    public static final String KAFKA_EVENT_PAYLOAD_REQUIRED = "event.payload is required";
    public static final String OUTBOX_MAX_ATTEMPTS_POSITIVE = "maxAttempts must be positive";
    public static final String OUTBOX_BASE_DELAY_REQUIRED = "baseDelay is required";
    public static final String OUTBOX_MAX_DELAY_REQUIRED = "maxDelay is required";
    public static final String OUTBOX_BASE_DELAY_POSITIVE = "baseDelay must be positive";
    public static final String OUTBOX_MAX_DELAY_NOT_LESS_THAN_BASE =
            "maxDelay must be greater than or equal to baseDelay";
    public static final String OUTBOX_ID_REQUIRED = "id is required";
    public static final String OUTBOX_EVENT_TYPE_REQUIRED = "eventType is required";
    public static final String OUTBOX_AGGREGATE_TYPE_REQUIRED = "aggregateType is required";
    public static final String OUTBOX_AGGREGATE_ID_REQUIRED = "aggregateId is required";
    public static final String OUTBOX_PAYLOAD_REQUIRED = "payload is required";
    public static final String OUTBOX_OCCURRED_AT_REQUIRED = "occurredAt is required";
    public static final String OUTBOX_SERIALIZATION_FAILED = "Outbox payload serialization failed";
    public static final String OUTBOX_DESERIALIZATION_FAILED =
            "Outbox payload deserialization failed";
    public static final String CORRELATION_ID_REQUIRED = "correlationId must not be blank";
    public static final String OBSERVATION_REGISTRY_REQUIRED = "observationRegistry is required";
    public static final String SERVICE_ACCESS_TOKEN_MISSING =
            "Keycloak returned no service access token";
    public static final String CLIENT_ASSERTION_SIGN_FAILED =
            "Unable to sign Keycloak client assertion";
    public static final String CLIENT_PRIVATE_KEY_NOT_RSA =
            "Configured client private key is not RSA CRT key material";
    public static final String CLIENT_PRIVATE_KEY_LOAD_FAILED =
            "Unable to load Keycloak client private key";
    public static final String CLIENT_PUBLIC_KEY_DERIVATION_FAILED =
            "Unable to derive Keycloak client public key";
}
