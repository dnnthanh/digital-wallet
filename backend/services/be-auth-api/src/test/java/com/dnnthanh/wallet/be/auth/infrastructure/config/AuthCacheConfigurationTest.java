package com.dnnthanh.wallet.be.auth.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;
import com.dnnthanh.wallet.be.platform.cache.RedisCacheSpec;
import com.dnnthanh.wallet.be.platform.cache.WalletCacheNames;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class AuthCacheConfigurationTest {
    @Test
    void shouldRegisterBoundedTypedAuthorizationCache() {
        AuthCacheConfiguration configuration = new AuthCacheConfiguration(Duration.ofMinutes(2));

        RedisCacheSpec spec = configuration.authorizationCacheSpec();

        assertThat(spec.name()).isEqualTo(WalletCacheNames.AUTHORIZATION_SNAPSHOTS);
        assertThat(spec.ttl()).isEqualTo(Duration.ofMinutes(2));
        assertThat(spec.valueType()).isEqualTo(EffectiveAuthorization.class);
    }
}
