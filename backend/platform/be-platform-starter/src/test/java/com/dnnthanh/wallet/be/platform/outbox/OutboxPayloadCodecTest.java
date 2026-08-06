package com.dnnthanh.wallet.be.platform.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class OutboxPayloadCodecTest {

    @Test
    void roundTripsTypedPayloadAtInfrastructureBoundary() {
        var codec = new OutboxPayloadCodec(JsonMapper.builder().findAndAddModules().build());
        var payload = new SamplePayload("wallet-1", new BigDecimal("125000.00"));

        String json = codec.write(payload);

        assertThat(codec.read(json, SamplePayload.class)).isEqualTo(payload);
    }

    private record SamplePayload(String walletId, BigDecimal amount) {}
}
