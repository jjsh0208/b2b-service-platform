package com.devsquad10.order.infrastructure.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisTemplateConfig {

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);

		// String 타입은 StringRedisSerializer 사용
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		// Object 타입의 값은 StringRedisSerializer 사용
		redisTemplate.setValueSerializer(new StringRedisSerializer());

		return redisTemplate;
	}
}
