package com.dnnthanh.wallet.be.platform.outbox;

import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_DESERIALIZATION_FAILED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_SERIALIZATION_FAILED;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OutboxPayloadCodec {
    private final ObjectMapper objectMapper;

    public String write(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception failure) {
            throw new IllegalArgumentException(OUTBOX_SERIALIZATION_FAILED, failure);
        }
    }

    public <T> T read(String payload, Class<T> payloadType) {
        try {
            return objectMapper.readValue(payload, payloadType);
        } catch (Exception failure) {
            throw new IllegalArgumentException(OUTBOX_DESERIALIZATION_FAILED, failure);
        }
    }
}
