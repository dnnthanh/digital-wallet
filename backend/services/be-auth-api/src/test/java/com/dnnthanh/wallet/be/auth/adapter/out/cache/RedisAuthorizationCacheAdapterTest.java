package com.dnnthanh.wallet.be.auth.adapter.out.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;
import com.dnnthanh.wallet.be.platform.cache.WalletCacheManager;
import com.dnnthanh.wallet.be.platform.cache.WalletCacheNames;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RedisAuthorizationCacheAdapterTest {
    @Mock private WalletCacheManager cacheManager;

    @Test
    void shouldUseTypedAuthorizationSnapshotCache() {
        EffectiveAuthorization value =
                new EffectiveAuthorization(
                        "user-1", Set.of("self:read"), Set.of("/bank/demo-branch"));
        when(cacheManager.get(
                        WalletCacheNames.AUTHORIZATION_SNAPSHOTS,
                        "user-1",
                        EffectiveAuthorization.class))
                .thenReturn(Optional.of(value));
        RedisAuthorizationCacheAdapter adapter = new RedisAuthorizationCacheAdapter(cacheManager);

        assertThat(adapter.find("user-1")).contains(value);
        adapter.put("user-1", value);
        adapter.evict("user-1");

        verify(cacheManager).put(WalletCacheNames.AUTHORIZATION_SNAPSHOTS, "user-1", value);
        verify(cacheManager).evict(WalletCacheNames.AUTHORIZATION_SNAPSHOTS, "user-1");
    }
}
