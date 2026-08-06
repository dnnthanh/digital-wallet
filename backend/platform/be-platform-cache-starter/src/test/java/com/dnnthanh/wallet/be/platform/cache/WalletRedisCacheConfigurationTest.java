package com.dnnthanh.wallet.be.platform.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class WalletRedisCacheConfigurationTest {

    @Test
    void usesSpringApplicationNameAsReadableCacheNamespace() {
        var configuration =
                new WalletRedisCacheAutoConfiguration(
                        JsonMapper.builder().findAndAddModules().build(), "be-platform-cache-test");

        assertThat(configuration.cachePrefix("authorization-snapshots"))
                .isEqualTo("be-platform-cache-test::authorization-snapshots::");
    }
}
