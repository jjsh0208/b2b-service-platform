package com.devsquad10.product.infrastructure.config.redis;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.devsquad10.product.application.dto.PageProductResponseDto;
import com.devsquad10.product.application.dto.ProductResDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisCachingConfig {

	public static final String PRODUCT_CACHE = "productCache";
	public static final String PRODUCT_SEARCH_CACHE = "productSearchCache";

	private static final Duration DEFAULT_TTL = Duration.ofSeconds(120);
	private static final Duration PRODUCT_TTL = Duration.ofMinutes(10);
	private static final Duration PRODUCT_SEARCH_TTL = Duration.ofHours(1);

	@Bean
	public ObjectMapper redisObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper;
	}

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

		RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration
			.defaultCacheConfig()
			.entryTtl(DEFAULT_TTL)
			.disableCachingNullValues()
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
				new Jackson2JsonRedisSerializer<>(ProductResDto.class)));

		RedisCacheConfiguration orderConfiguration = RedisCacheConfiguration
			.defaultCacheConfig()
			.entryTtl(PRODUCT_TTL)
			.disableCachingNullValues()
			.computePrefixWith(CacheKeyPrefix.simple())
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
				new Jackson2JsonRedisSerializer<>(ProductResDto.class)));

		// OrderSearchCache 설정 (1시간 TTL, OrderSearchResDto 직렬화)
		RedisCacheConfiguration orderSearchConfiguration = RedisCacheConfiguration
			.defaultCacheConfig()
			.entryTtl(PRODUCT_SEARCH_TTL)
			.disableCachingNullValues()
			.computePrefixWith(CacheKeyPrefix.simple())
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
				new Jackson2JsonRedisSerializer<>(PageProductResponseDto.class)));

		// 각 캐시 별로 설정을 관리하는 Map을 생성
		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
		cacheConfigurations.put(PRODUCT_CACHE, orderConfiguration);
		cacheConfigurations.put(PRODUCT_SEARCH_CACHE, orderSearchConfiguration);

		return RedisCacheManager
			.builder(redisConnectionFactory)
			.cacheDefaults(defaultConfiguration)
			.withInitialCacheConfigurations(cacheConfigurations)
			.build();
	}
}
