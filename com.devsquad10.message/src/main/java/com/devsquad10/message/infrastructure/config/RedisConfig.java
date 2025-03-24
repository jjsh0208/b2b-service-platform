package com.devsquad10.message.infrastructure.config;

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

import com.devsquad10.message.application.dto.res.MessageGetOneResponseDto;
import com.devsquad10.message.application.dto.res.PagedMessageResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig {

	public static final String MESSAGE_CACHE = "messageCache";
	public static final String MESSAGE_SEARCH_CACHE = "messageSearchCache";

	private static final Duration DEFAULT_TTL = Duration.ofSeconds(120);
	private static final Duration MESSAGE_TTL = Duration.ofSeconds(300);
	private static final Duration MESSAGE_SEARCH_TTL = Duration.ofHours(1);

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

		RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(DEFAULT_TTL)
			.disableCachingNullValues()
			.serializeKeysWith(RedisSerializationContext.SerializationPair
				.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair
				.fromSerializer(RedisSerializer.java()));

		RedisCacheConfiguration customCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
			.disableCachingNullValues()
			.computePrefixWith(CacheKeyPrefix.simple())
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));

		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

		cacheConfigurations.put(MESSAGE_CACHE, customCacheConfiguration.entryTtl(MESSAGE_TTL)
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(
					customSerializer(objectMapper, MessageGetOneResponseDto.class)
				)));

		cacheConfigurations.put(MESSAGE_SEARCH_CACHE, customCacheConfiguration.entryTtl(MESSAGE_SEARCH_TTL)
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(
					customSerializer(objectMapper, PagedMessageResponseDto.class)
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
