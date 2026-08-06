package com.dnnthanh.wallet.be.platform.cache;

import static com.dnnthanh.wallet.be.platform.cache.CacheInvariantMessages.CACHE_NAME_REQUIRED;
import static com.dnnthanh.wallet.be.platform.cache.CacheInvariantMessages.CACHE_TTL_POSITIVE;
import static com.dnnthanh.wallet.be.platform.cache.CacheInvariantMessages.CACHE_TTL_REQUIRED;
import static com.dnnthanh.wallet.be.platform.cache.CacheInvariantMessages.CACHE_VALUE_TYPE_REQUIRED;

import java.time.Duration;
import org.apache.commons.lang3.Validate;

/** Defines one typed Redis-backed Spring cache. */
public record RedisCacheSpec(String name, Duration ttl, Class<?> valueType) {
    public RedisCacheSpec {
        Validate.notBlank(name, CACHE_NAME_REQUIRED);
        Validate.notNull(ttl, CACHE_TTL_REQUIRED);
        Validate.notNull(valueType, CACHE_VALUE_TYPE_REQUIRED);
        Validate.isTrue(!ttl.isZero() && !ttl.isNegative(), CACHE_TTL_POSITIVE);
    }
}
