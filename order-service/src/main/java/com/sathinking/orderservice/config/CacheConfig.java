package com.sathinking.orderservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sathinking.orderservice.dto.OrderResponse;
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
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        Jackson2JsonRedisSerializer<OrderResponse> orderSerializer =
                new Jackson2JsonRedisSerializer<>(mapper, OrderResponse.class);

        RedisCacheConfiguration orderCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(orderSerializer));

        return builder -> builder.withCacheConfiguration("orders", orderCacheConfig);
    }
}