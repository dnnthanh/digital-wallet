package com.dnnthanh.wallet.be.platform.cache;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheInvariantMessages {
    public static final String CACHE_NAME_REQUIRED = "cache name is required";
    public static final String CACHE_TTL_REQUIRED = "cache ttl is required";
    public static final String CACHE_VALUE_TYPE_REQUIRED = "cache value type is required";
    public static final String CACHE_TTL_POSITIVE = "cache ttl must be positive";
    public static final String CACHE_NOT_CONFIGURED_PREFIX = "cache is not configured: ";
}
