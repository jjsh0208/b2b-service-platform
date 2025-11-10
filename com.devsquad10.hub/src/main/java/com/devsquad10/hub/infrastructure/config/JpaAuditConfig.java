package com.devsquad10.hub.infrastructure.config;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
	@Bean
	public AuditorAware<UUID> auditorProvider() {
		return new AuditorAwareImpl();
	}
}
