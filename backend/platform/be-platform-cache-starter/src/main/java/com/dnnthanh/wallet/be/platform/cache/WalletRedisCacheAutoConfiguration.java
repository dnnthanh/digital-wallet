package com.dnnthanh.wallet.be.platform.cache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@AutoConfigureAfter(DataRedisAutoConfiguration.class)
@AutoConfigureBefore(CacheAutoConfiguration.class)
@EnableCaching
@ConditionalOnClass(RedisCacheManager.class)
@ConditionalOnBean(RedisConnectionFactory.class)
public class WalletRedisCacheAutoConfiguration implements CachingConfigurer {
    private final ObjectMapper objectMapper;
    private final String applicationName;

    public WalletRedisCacheAutoConfiguration(
            ObjectMapper objectMapper,
            @Value("${spring.application.name}") String applicationName) {
        Assert.hasText(applicationName, "spring.application.name must not be blank");
        this.objectMapper = objectMapper;
        this.applicationName = applicationName.trim();
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public RedisCacheManager walletRedisCacheManager(
            RedisConnectionFactory connectionFactory, List<RedisCacheSpec> specs) {
        Map<String, RedisCacheConfiguration> configurations = new LinkedHashMap<>();
        for (RedisCacheSpec spec : specs) {
            configurations.put(spec.name(), cacheConfiguration(spec));
        }
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration())
                .withInitialCacheConfigurations(configurations)
                .allowCreateOnMissingCache(false)
                .enableStatistics()
                .transactionAware()
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public WalletCacheManager walletCacheManager(CacheManager cacheManager) {
        return new WalletCacheManager(cacheManager);
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new FailOpenCacheErrorHandler();
    }

    String cachePrefix(String cacheName) {
        return applicationName + "::" + cacheName + "::";
    }

    private RedisCacheConfiguration defaultConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .computePrefixWith(this::cachePrefix)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private RedisCacheConfiguration cacheConfiguration(RedisCacheSpec spec) {
        RedisSerializer serializer = new JacksonJsonRedisSerializer(objectMapper, spec.valueType());
        return defaultConfiguration()
                .entryTtl(spec.ttl())
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }
}
