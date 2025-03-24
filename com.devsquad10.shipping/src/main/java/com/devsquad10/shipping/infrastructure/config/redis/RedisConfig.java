package com.devsquad10.shipping.infrastructure.config.redis;

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
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.devsquad10.shipping.application.dto.response.PagedShippingResDto;
import com.devsquad10.shipping.application.dto.response.PagedShippingAgentResDto;
import com.devsquad10.shipping.application.dto.response.PagedShippingHistoryResDto;
import com.devsquad10.shipping.application.dto.response.ShippingAgentResDto;
import com.devsquad10.shipping.application.dto.response.ShippingHistoryResDto;
import com.devsquad10.shipping.application.dto.response.ShippingResDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig {

	private static final String SHIPPING_CACHE = "shippingCache";
	private static final String SHIPPING_SEARCH_CACHE = "shippingSearchCache";
	private static final String SHIPPING_HISTORY_CACHE = "shippingHistoryCache";
	private static final String SHIPPING_HISTORY_SEARCH_CACHE = "shippingHistorySearchCache";
	private static final String SHIPPING_AGENT_CACHE = "shippingAgentCache";
	private static final String SHIPPING_AGENT_SEARCH_CACHE = "shippingAgentSearchCache";

	private static final Duration DEFAULT_TTL = Duration.ofSeconds(120);
	private static final Duration SHIPPING_TTL = Duration.ofSeconds(300);
	private static final Duration SHIPPING_SEARCH_TTL = Duration.ofHours(1);
	private static final Duration SHIPPING_HISTORY_TTL = Duration.ofSeconds(300);
	private static final Duration SHIPPING_HISTORY_SEARCH_TTL = Duration.ofHours(1);
	private static final Duration SHIPPING_AGENT_TTL = Duration.ofSeconds(300);
	private static final Duration SHIPPING_AGENT_SEARCH_TTL = Duration.ofHours(1);

	@Bean
	public ObjectMapper redisObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper;
	}

	@Bean
	public RedisCacheManager cacheManager(
		RedisConnectionFactory redisConnectionFactory
	) {
		ObjectMapper objectMapper = redisObjectMapper();

		// default cache 설정
		RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(DEFAULT_TTL)
			.disableCachingNullValues()
			.serializeKeysWith(RedisSerializationContext.SerializationPair
				.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair
				.fromSerializer(RedisSerializer.java()));

		// custom cache 설정
		RedisCacheConfiguration customCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
			.disableCachingNullValues()
			.computePrefixWith(CacheKeyPrefix.simple())
			.serializeKeysWith(RedisSerializationContext.SerializationPair
				.fromSerializer(new StringRedisSerializer()));

		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
		cacheConfigurations.put(SHIPPING_CACHE, customCacheConfiguration
			.entryTtl(SHIPPING_TTL)
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
				customSerializer(objectMapper, ShippingResDto.class)
			)));

		cacheConfigurations.put(SHIPPING_SEARCH_CACHE, customCacheConfiguration
			.entryTtl(SHIPPING_SEARCH_TTL)
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(
					customSerializer(objectMapper, PagedShippingResDto.class)
				)));

		cacheConfigurations.put(SHIPPING_HISTORY_CACHE, customCacheConfiguration
			.entryTtl(SHIPPING_HISTORY_TTL)
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
				customSerializer(objectMapper, ShippingHistoryResDto.class)
			)));

		cacheConfigurations.put(SHIPPING_HISTORY_SEARCH_CACHE, customCacheConfiguration
			.entryTtl(SHIPPING_HISTORY_SEARCH_TTL)
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(
					customSerializer(objectMapper, PagedShippingHistoryResDto.class)
				)));

		cacheConfigurations.put(SHIPPING_AGENT_CACHE, customCacheConfiguration
			.entryTtl(SHIPPING_AGENT_TTL)
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
				customSerializer(objectMapper, ShippingAgentResDto.class)
			)));

		cacheConfigurations.put(SHIPPING_AGENT_SEARCH_CACHE, customCacheConfiguration
			.entryTtl(SHIPPING_AGENT_SEARCH_TTL)
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(
					customSerializer(objectMapper, PagedShippingAgentResDto.class)
				)));

		return RedisCacheManager.builder(redisConnectionFactory)
			.cacheDefaults(cacheConfiguration)
			.withInitialCacheConfigurations(cacheConfigurations)
			.build();
	}

	private <T> Jackson2JsonRedisSerializer<T> customSerializer(ObjectMapper objectMapper, Class<T> type) {
		return new Jackson2JsonRedisSerializer<>(objectMapper, type);
	}
}
