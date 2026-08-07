package com.dnnthanh.wallet.be.platform.autoconfigure;

import com.dnnthanh.wallet.be.platform.outbox.OutboxPayloadCodec;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

/** Digital Wallet platform auto-configuration for shared transactional-outbox support. */
@AutoConfiguration
public class PlatformOutboxAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    OutboxPayloadCodec outboxPayloadCodec(ObjectMapper objectMapper) {
        return new OutboxPayloadCodec(objectMapper);
    }
}
