package com.dnnthanh.wallet.be.platform.autoconfigure;

import com.dnnthanh.wallet.be.platform.outbox.OutboxPayloadCodec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

/** Digital Wallet platform configuration for shared transactional-outbox support. */
@Configuration(proxyBeanMethods = false)
public class PlatformOutboxAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    OutboxPayloadCodec outboxPayloadCodec(ObjectMapper objectMapper) {
        return new OutboxPayloadCodec(objectMapper);
    }
}
