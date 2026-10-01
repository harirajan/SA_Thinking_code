package com.sathinking.productservice.config;

import com.sathinking.productservice.dto.ProductResponse;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        Jackson2JsonRedisSerializer<ProductResponse> productSerializer =
                new Jackson2JsonRedisSerializer<>(ProductResponse.class);

        RedisCacheConfiguration productCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(productSerializer));

        return builder -> builder.withCacheConfiguration("products", productCacheConfig);
    }
}