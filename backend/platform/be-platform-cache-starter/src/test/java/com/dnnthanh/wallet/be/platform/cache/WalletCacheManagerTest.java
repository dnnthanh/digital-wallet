package com.dnnthanh.wallet.be.platform.cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class WalletCacheManagerTest {
    @Test
    void readsWritesAndEvictsTypedValues() {
        var delegate = new ConcurrentMapCacheManager("authorization-snapshots");
        var cache = new WalletCacheManager(delegate);
        cache.put("authorization-snapshots", "user-1", new Snapshot("OPERATIONS"));
        assertThat(cache.get("authorization-snapshots", "user-1", Snapshot.class))
                .contains(new Snapshot("OPERATIONS"));
        cache.evict("authorization-snapshots", "user-1");
        assertThat(cache.get("authorization-snapshots", "user-1", Snapshot.class)).isEmpty();
    }

    record Snapshot(String role) {}
}
