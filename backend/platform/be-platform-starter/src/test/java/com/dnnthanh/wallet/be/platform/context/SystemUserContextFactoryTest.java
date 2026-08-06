package com.dnnthanh.wallet.be.platform.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SystemUserContextFactoryTest {
    private final SystemUserContextFactory factory = new SystemUserContextFactory();

    @Test
    void shouldCreateKafkaConsumerTechnicalActor() {
        UserContext context = factory.forKafkaConsumer("be-payment-worker");

        assertThat(context.userId()).isEqualTo("be-payment-worker");
        assertThat(context.username()).isEqualTo("be-payment-worker");
        assertThat(context.actorType()).isEqualTo(UserContext.ActorType.KAFKA_CONSUMER);
        assertThat(context.roles()).containsExactly("SERVICE_ACCOUNT");
    }

    @Test
    void shouldCreateSchedulerTechnicalActor() {
        assertThat(factory.forScheduler("be-payment-job").actorType())
                .isEqualTo(UserContext.ActorType.SCHEDULER);
    }

    @Test
    void shouldCreateServiceTechnicalActor() {
        assertThat(factory.forService("be-auth-api").actorType())
                .isEqualTo(UserContext.ActorType.SERVICE);
    }
}
