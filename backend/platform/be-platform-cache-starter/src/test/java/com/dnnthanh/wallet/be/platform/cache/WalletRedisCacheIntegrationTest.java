package com.dnnthanh.wallet.be.platform.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.json.JsonMapper;

@Testcontainers(disabledWithoutDocker = true)
class WalletRedisCacheIntegrationTest {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
                    .withExposedPorts(6379);

    static LettuceConnectionFactory connectionFactory;

    @BeforeAll
    static void connect() {
        connectionFactory =
                new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
    }

    @AfterAll
    static void disconnect() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @Test
    void storesTypedObjectAndUsesApplicationNamePrefix() {
        var autoConfiguration =
                new WalletRedisCacheAutoConfiguration(
                        JsonMapper.builder().findAndAddModules().build(), "be-platform-cache-test");
        var redisCacheManager =
                autoConfiguration.walletRedisCacheManager(
                        connectionFactory,
                        List.of(
                                new RedisCacheSpec(
                                        "sample", Duration.ofMinutes(1), SampleValue.class)));
        redisCacheManager.afterPropertiesSet();
        var cacheManager = new WalletCacheManager(redisCacheManager);

        var expected = new SampleValue("wallet-1", "ACTIVE");
        cacheManager.put("sample", "wallet-1", expected);

        assertThat(cacheManager.get("sample", "wallet-1", SampleValue.class)).contains(expected);

        try (var connection = connectionFactory.getConnection()) {
            Set<byte[]> rawKeys =
                    connection.keyCommands().keys("*".getBytes(StandardCharsets.UTF_8));
            assertThat(rawKeys)
                    .extracting(bytes -> new String(bytes, StandardCharsets.UTF_8))
                    .contains("be-platform-cache-test::sample::wallet-1");
        }
    }

    private record SampleValue(String walletId, String status) {}
}
