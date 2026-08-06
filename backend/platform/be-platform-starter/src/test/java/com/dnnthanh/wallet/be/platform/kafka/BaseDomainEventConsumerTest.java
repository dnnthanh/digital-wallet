package com.dnnthanh.wallet.be.platform.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.dnnthanh.wallet.be.platform.context.UserContext;
import com.dnnthanh.wallet.be.platform.event.EventEnvelope;
import org.junit.jupiter.api.Test;

class BaseDomainEventConsumerTest {

    @Test
    void exposesTypedPayloadWithoutMapLookups() {
        var event =
                EventEnvelope.create(
                        "wallet.test.created.v1",
                        "wallet-1",
                        "trace-1",
                        new SamplePayload("wallet-1", 2));

        var consumer = new SampleConsumer();

        assertThat(consumer.payloadOf(event)).isEqualTo(new SamplePayload("wallet-1", 2));
    }

    @Test
    void exposesExplicitKafkaTechnicalContextWithoutHttpSecurityContext() {
        var consumer = new SampleConsumer();

        UserContext context = consumer.systemContext("be-payment-worker");

        assertThat(context.userId()).isEqualTo("be-payment-worker");
        assertThat(context.actorType()).isEqualTo(UserContext.ActorType.KAFKA_CONSUMER);
        assertThat(context.roles()).containsExactly("SERVICE_ACCOUNT");
    }

    private record SamplePayload(String walletId, int version) {}

    private static final class SampleConsumer extends BaseDomainEventConsumer<SamplePayload> {
        SamplePayload payloadOf(EventEnvelope<SamplePayload> event) {
            return payload(event);
        }

        UserContext systemContext(String serviceName) {
            return systemUserContext(serviceName);
        }
    }
}
