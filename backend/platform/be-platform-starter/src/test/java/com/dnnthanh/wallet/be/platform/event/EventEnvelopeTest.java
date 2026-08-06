package com.dnnthanh.wallet.be.platform.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class EventEnvelopeTest {
    @Test
    void serializesReferenceStyleEventMetadata() throws Exception {
        var envelope =
                EventEnvelope.create(
                        "wallet.test.created.v1", "wallet-1", "trace-1", Map.of("id", "wallet-1"));

        String json = JsonMapper.builder().findAndAddModules().build().writeValueAsString(envelope);

        assertThat(json).contains("wallet.test.created.v1", "wallet-1", "trace-1", "eventVersion");
        assertThat(envelope.eventVersion()).isEqualTo(1);
        assertThat(envelope.correlationId()).isEqualTo("trace-1");
    }
}
