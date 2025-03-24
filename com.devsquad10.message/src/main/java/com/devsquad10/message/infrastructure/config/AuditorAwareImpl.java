package com.devsquad10.message.infrastructure.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.AuditorAware;

import jakarta.annotation.Nonnull;

public class AuditorAwareImpl implements AuditorAware<UUID> {
	@Nonnull
	@Override
	public Optional<UUID> getCurrentAuditor() {
		return Optional.empty();
	}
}
