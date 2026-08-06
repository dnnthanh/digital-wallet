package com.dnnthanh.wallet.be.auth.adapter.out.cache;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;
import com.dnnthanh.wallet.be.auth.application.port.out.AuthorizationCachePort;
import com.dnnthanh.wallet.be.platform.cache.WalletCacheManager;
import com.dnnthanh.wallet.be.platform.cache.WalletCacheNames;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisAuthorizationCacheAdapter implements AuthorizationCachePort {
    private final WalletCacheManager cacheManager;

    @Override
    public Optional<EffectiveAuthorization> find(String userId) {
        return cacheManager.get(
                WalletCacheNames.AUTHORIZATION_SNAPSHOTS, userId, EffectiveAuthorization.class);
    }

    @Override
    public void put(String userId, EffectiveAuthorization authorization) {
        cacheManager.put(WalletCacheNames.AUTHORIZATION_SNAPSHOTS, userId, authorization);
    }

    @Override
    public void evict(String userId) {
        cacheManager.evict(WalletCacheNames.AUTHORIZATION_SNAPSHOTS, userId);
    }
}
