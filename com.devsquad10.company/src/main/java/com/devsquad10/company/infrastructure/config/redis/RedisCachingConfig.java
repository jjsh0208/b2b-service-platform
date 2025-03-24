package com.devsquad10.company.infrastructure.config.redis;

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

import com.devsquad10.company.application.dto.CompanyResDto;
import com.devsquad10.company.application.dto.PageCompanyResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisCachingConfig {

	public static final String COMPANY_CACHE = "companyCache";
	public static final String COMPANY_SEARCH_CACHE = "companySearchCache";

	private static final Duration DEFAULT_TTL = Duration.ofSeconds(120);
	private static final Duration COMPANY_TTL = Duration.ofMinutes(10);
	private static final Duration COMPANY_SEARCH_TTL = Duration.ofHours(1);

	@Bean
	public ObjectMapper redisObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper;
	}

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
		ObjectMapper objectMapper = redisObjectMapper();

		Jackson2JsonRedisSerializer<CompanyResDto> companySerializer = new Jackson2JsonRedisSerializer<>(
			CompanyResDto.class);

		// 기본값 캐시 설정 (2분 TTL, JSON 직렬화 방식)
		RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration
			.defaultCacheConfig()
			.entryTtl(DEFAULT_TTL)
			.disableCachingNullValues()
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
				new Jackson2JsonRedisSerializer<>(CompanyResDto.class)));

		// companyCache 에 대한 캐시 설정 (10분 TTL, JSON 직렬화 방식)
		RedisCacheConfiguration companyConfiguration = RedisCacheConfiguration
			.defaultCacheConfig()
			.entryTtl(COMPANY_TTL)
			.disableCachingNullValues()
			.computePrefixWith(CacheKeyPrefix.simple())
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
				new Jackson2JsonRedisSerializer<>(CompanyResDto.class)));

		// companySearchCache 에 대한 캐시 설정 (1시간 TTL, JSON 직렬화 방식)
		RedisCacheConfiguration companySearchConfiguration = RedisCacheConfiguration
			.defaultCacheConfig()
			.entryTtl(COMPANY_SEARCH_TTL)
			.disableCachingNullValues()
			.computePrefixWith(CacheKeyPrefix.simple())
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
				new Jackson2JsonRedisSerializer<>(PageCompanyResponseDto.class)));

		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
		cacheConfigurations.put(COMPANY_CACHE, companyConfiguration);
		cacheConfigurations.put(COMPANY_SEARCH_CACHE, companySearchConfiguration);

		return RedisCacheManager
			.builder(redisConnectionFactory)
			.cacheDefaults(defaultConfiguration)
			.withInitialCacheConfigurations(cacheConfigurations)
			.build();
	}
}
