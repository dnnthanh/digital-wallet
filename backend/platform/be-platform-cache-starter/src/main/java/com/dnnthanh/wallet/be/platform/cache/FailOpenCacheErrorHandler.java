package com.dnnthanh.wallet.be.platform.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/** Keeps Redis/cache failures from becoming authoritative business-data failures. */
@Slf4j
public final class FailOpenCacheErrorHandler implements CacheErrorHandler {
    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log("get", exception, cache, key);
    }

    @Override
    public void handleCachePutError(
            RuntimeException exception, Cache cache, Object key, Object value) {
        log("put", exception, cache, key);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log("evict", exception, cache, key);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log("clear", exception, cache, "*");
    }

    private void log(String operation, RuntimeException exception, Cache cache, Object key) {
        log.warn(
                "cache_operation_failed operation={} cache={} keyHash={} failure={}",
                operation,
                cache.getName(),
                "*".equals(key) ? "*" : java.util.Objects.hashCode(key),
                exception.toString());
    }
}
