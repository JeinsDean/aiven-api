package com.jeinsdean.aiven.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 캐시 설정
 *
 * AI 응답 캐싱으로 비용 최적화:
 * - 동일한 컨텍스트 요청은 캐시된 응답 반환
 * - AI API 호출 횟수 감소 = 비용 절감
 * - 응답 속도 향상 (수 초 → 수십 ms)
 *
 * 캐시 전략:
 * - AI 응답: 1시간 (컨텍스트 변화 고려)
 * - 사용자 정보: 5분
 * - 위치 정보: 10분
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis Cache Manager 설정
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 기본 TTL: 1시간
                .entryTtl(Duration.ofHours(1))

                // Null 값 캐싱 방지
                .disableCachingNullValues()

                // Key Serializer: String
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )

                // Value Serializer: JSON
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper()))
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                // 캐시별 개별 설정
                .withCacheConfiguration("aiResponse",
                        config.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("userInfo",
                        config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("locationInfo",
                        config.entryTtl(Duration.ofMinutes(10)))
                .build();
    }

    /**
     * Redis JSON 직렬화용 ObjectMapper
     * LocalDateTime 등 Java 8 타입 지원
     */
    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}