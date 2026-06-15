package com.example.issuetracker.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@Slf4j
public class RedisConfig implements CachingConfigurer {

    @Bean
    RedisCacheManagerBuilderCustomizer cacheCustomizer() {
        var serializer = redisSerializer();
        var pair = RedisSerializationContext.SerializationPair.fromSerializer(serializer);
        return builder -> builder
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(pair)
                        .disableCachingNullValues())
                .withCacheConfiguration("ticket-detail",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(5))
                                .serializeValuesWith(pair));
    }

    static GenericJackson2JsonRedisSerializer redisSerializer() {
        var validator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.example.issuetracker.")
                .allowIfSubType("java.lang.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.util.")
                .build();
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                validator,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn(
                        "Ignoring unreadable cache entry cache={} key={}: {}",
                        cache.getName(),
                        key,
                        exception.getMessage()
                );
                try {
                    cache.evict(key);
                } catch (RuntimeException evictionException) {
                    log.warn(
                            "Failed to evict unreadable cache entry cache={} key={}",
                            cache.getName(),
                            key,
                            evictionException
                    );
                }
            }
        };
    }
}

