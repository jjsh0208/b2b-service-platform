package com.devsquad10.hub.infrastructure.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;

public class AuditorAwareImpl implements AuditorAware<UUID> {

	// 배치 작업이나 테스트용 시스템 사용자 ID
	private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	@Nonnull
	@Override
	public Optional<UUID> getCurrentAuditor() {

		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();

		if (servletRequestAttributes != null) {
			HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();

			String userHeader = httpServletRequest.getHeader("X-User-Id");

			if (userHeader != null) {
				return Optional.of(UUID.fromString(userHeader));
			}
		}

		return Optional.of(SYSTEM_USER_ID);
	}
}
