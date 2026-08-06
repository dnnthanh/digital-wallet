package com.dnnthanh.wallet.be.auth.infrastructure.config;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;
import com.dnnthanh.wallet.be.platform.cache.RedisCacheSpec;
import com.dnnthanh.wallet.be.platform.cache.WalletCacheNames;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthCacheConfiguration {
    private final Duration cacheTtl;

    public AuthCacheConfiguration(@Value("${wallet.auth.cache-ttl:PT2M}") Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    @Bean
    public RedisCacheSpec authorizationCacheSpec() {
        return new RedisCacheSpec(
                WalletCacheNames.AUTHORIZATION_SNAPSHOTS, cacheTtl, EffectiveAuthorization.class);
    }
}
