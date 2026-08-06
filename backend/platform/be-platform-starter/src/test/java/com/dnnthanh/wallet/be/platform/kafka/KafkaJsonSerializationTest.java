package com.dnnthanh.wallet.be.platform.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.dnnthanh.wallet.be.platform.event.EventEnvelope;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class KafkaJsonSerializationTest {

    @Test
    void roundTripsTypedEventEnvelopeThroughSpringKafkaJacksonSerde() {
        var mapper = JsonMapper.builder().findAndAddModules().build();
        var serializer = new JacksonJsonSerializer<EventEnvelope<SamplePayload>>(mapper);
        var deserializer =
                new JacksonJsonDeserializer<EventEnvelope<SamplePayload>>(
                        new TypeReference<EventEnvelope<SamplePayload>>() {}, mapper);
        var event =
                EventEnvelope.create(
                        "wallet.transfer.completed.v1",
                        "transfer-1",
                        "trace-1",
                        new SamplePayload("transfer-1", new BigDecimal("100000.00")));

        byte[] bytes = serializer.serialize("wallet.transfer.completed.v1", event);
        EventEnvelope<SamplePayload> decoded =
                deserializer.deserialize("wallet.transfer.completed.v1", bytes);

        assertThat(decoded.eventType()).isEqualTo(event.eventType());
        assertThat(decoded.payload()).isEqualTo(event.payload());
    }

    private record SamplePayload(String transferId, BigDecimal amount) {}
}
