package com.dnnthanh.wallet.be.platform.cache;

import static com.dnnthanh.wallet.be.platform.cache.CacheInvariantMessages.CACHE_NOT_CONFIGURED_PREFIX;
import static com.dnnthanh.wallet.be.platform.cache.CacheInvariantMessages.CACHE_VALUE_TYPE_REQUIRED;

import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/** Generic typed cache facade adapted from the reference platform starter. */
@Slf4j
@RequiredArgsConstructor
public final class WalletCacheManager {
    private final CacheManager delegate;

    public <T> Optional<T> get(String cacheName, Object key, Class<T> valueType) {
        Objects.requireNonNull(valueType, CACHE_VALUE_TYPE_REQUIRED);
        try {
            return Optional.ofNullable(cache(cacheName).get(key, valueType));
        } catch (RuntimeException failure) {
            log.warn(
                    "cache_read_failed cache={} keyHash={} valueType={} failure={}",
                    cacheName,
                    Objects.hashCode(key),
                    valueType.getSimpleName(),
                    failure.toString());
            return Optional.empty();
        }
    }

    public <T> void put(String cacheName, Object key, T value) {
        try {
            cache(cacheName).put(key, value);
        } catch (RuntimeException failure) {
            log.warn(
                    "cache_write_failed cache={} keyHash={} valueType={} failure={}",
                    cacheName,
                    Objects.hashCode(key),
                    Objects.isNull(value) ? "null" : value.getClass().getSimpleName(),
                    failure.toString());
        }
    }

    public void evict(String cacheName, Object key) {
        try {
            cache(cacheName).evict(key);
        } catch (RuntimeException failure) {
            log.warn(
                    "cache_evict_failed cache={} keyHash={} failure={}",
                    cacheName,
                    Objects.hashCode(key),
                    failure.toString());
        }
    }

    private Cache cache(String cacheName) {
        return Objects.requireNonNull(
                delegate.getCache(cacheName), CACHE_NOT_CONFIGURED_PREFIX + cacheName);
    }
}
