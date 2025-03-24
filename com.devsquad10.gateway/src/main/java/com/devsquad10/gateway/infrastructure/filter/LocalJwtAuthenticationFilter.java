package com.devsquad10.gateway.infrastructure.filter;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LocalJwtAuthenticationFilter implements GlobalFilter {

	@Value("${service.jwt.secret-key}")
	private String secretKey;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();
		String method = exchange.getRequest().getMethod().toString();

		// 로그인 & 회원가입 API는 토큰 검증 제외
		if (path.equals("/api/user/signIn") || path.equals("/api/user/signup")) {
			return chain.filter(exchange);
		}

		// JWT 토큰 추출
		String token = extractToken(exchange);

		// 토큰이 없거나 유효하지 않으면 401 응답 반환
		if (token == null || !validateToken(token)) {
			log.warn("Unauthorized request - Missing or invalid token");
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		// JWT 디코딩 후 사용자 정보 추출
		Claims claims = decodeToken(token);
		if (claims == null) {
			log.warn("Unauthorized request - Invalid JWT claims");
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		exchange = addUserHeaders(exchange, claims);

		// 사용자 정보 헤더에 추가
		if (!isAuthorized(path, method, claims.get("role").toString())) {
			log.warn("Unauthorized access attempt to {} by role {}", path, claims.get("role"));
			exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
			return exchange.getResponse().setComplete();
		}

		return chain.filter(exchange);
	}

	private String extractToken(ServerWebExchange exchange) {
		String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (token != null && token.startsWith("Bearer ")) {
			return token.substring(7);
		}
		return null;
	}

	private boolean validateToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
			Jws<Claims> claimsJws = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token);

			// 토큰 만료 여부 확인
			if (claimsJws.getPayload().getExpiration().before(new Date())) {
				log.warn("Token expired: {}", claimsJws.getPayload().getExpiration());
				return false;
			}

			log.info("JWT validation success");
			return true;
		} catch (Exception e) {
			log.error("JWT validation failed: {}", e.getMessage());
			return false;
		}
	}

	private Claims decodeToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
			return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (Exception e) {
			log.error("Failed to decode JWT token: {}", e.getMessage());
			return null;
		}
	}

	private ServerWebExchange addUserHeaders(ServerWebExchange exchange, Claims claims) {
		return exchange.mutate()
			.request(exchange.getRequest().mutate()
				.header("X-User-Id", Objects.toString(claims.getSubject(), ""))
				.header("X-Slack-Id", Objects.toString(claims.get("slack_id"), ""))
				.header("X-User-Role", Objects.toString(claims.get("role"), ""))
				.build())
			.build();
	}

	private boolean isAuthorized(String path, String method, String role) {
		// API 경로와 메소드에 따른 권한 요구 사항 정의
		Map<String, Set<String>> roleRequirements = Map.ofEntries(
			Map.entry("/api/user/{id} GET", Set.of("ALL")),
			Map.entry("/api/user GET", Set.of("MASTER")),
			Map.entry("/api/user/search GET", Set.of("MASTER")),
			Map.entry("/api/user/{id} PATCH", Set.of("ALL")),
			Map.entry("/api/user/{id} DELETE", Set.of("ALL")),
			Map.entry("/api/hub POST", Set.of("MASTER")),
			Map.entry("/api/hub GET", Set.of("ALL")),
			Map.entry("/api/hub/{id} PATCH", Set.of("MASTER")),
			Map.entry("/api/hub/{id} DELETE", Set.of("MASTER")),
			Map.entry("/api/hub-route POST", Set.of("MASTER")),
			Map.entry("/api/hub-route/{id} GET", Set.of("ALL")),
			Map.entry("/api/hub-route/{id} PATCH", Set.of("MASTER")),
			Map.entry("/api/hub-route/{id} DELETE", Set.of("MASTER")),
			Map.entry("/api/message POST", Set.of("ALL")),
			Map.entry("/api/message/{id} GET", Set.of("MASTER")),
			Map.entry("/api/message/{id} PATCH", Set.of("MASTER")),
			Map.entry("/api/message/{id} DELETE", Set.of("MASTER")),
			Map.entry("/api/shipping POST", Set.of("MASTER")),
			Map.entry("/api/shipping/search GET", Set.of("ALL")),
			Map.entry("/api/shipping/{id} GET", Set.of("ALL")),
			Map.entry("/api/shipping/status-update/{id} PATCH", Set.of("DVL_OFFICER", "HUB", "MASTER")),
			Map.entry("/api/shipping/allocation/{id} PATCH", Set.of("HUB", "MASTER")),
			Map.entry("/api/shipping/order-update/{id} PATCH", Set.of("DVL_OFFICER", "HUB", "MASTER")),
			Map.entry("/api/shipping/{id} DELETE", Set.of("HUB", "MASTER")),
			Map.entry("/api/shipping-agent POST", Set.of("HUB", "MASTER")),
			Map.entry("/api/shipping-agent/{id} GET", Set.of("DVL_OFFICER", "MASTER")),
			Map.entry("/api/shipping-agent/search GET", Set.of("ALL")),
			Map.entry("/api/shipping-agent/info-update/{id} PATCH", Set.of("HUB", "MASTER")),
			Map.entry("/api/shipping-agent/transit-update/{id} PATCH", Set.of("DVL_OFFICER", "VEN_OFFICER")),
			Map.entry("/api/shipping-agent/{id} DELETE", Set.of("HUB", "MASTER")),
			Map.entry("/api/product POST", Set.of("ALL", "HUB", "MASTER", "VEN_OFFICER")),
			Map.entry("/api/product/{id} GET", Set.of("ALL")),
			Map.entry("/api/product/search GET", Set.of("ALL")),
			Map.entry("/api/product/{id} PATCH", Set.of("HUB", "MASTER", "VEN_OFFICER")),
			Map.entry("/api/product/{id} DELETE", Set.of("HUB", "MASTER")),
			Map.entry("/api/order POST", Set.of("ALL")),
			Map.entry("/api/order/{id} GET", Set.of("ALL")),
			Map.entry("/api/order/search GET", Set.of("ALL")),
			Map.entry("/api/order/{id} PATCH", Set.of("HUB", "MASTER")),
			Map.entry("/api/order/{id} DELETE", Set.of("HUB", "MASTER")),
			Map.entry("/api/company POST", Set.of("HUB", "MASTER")),
			Map.entry("/api/company/{id} GET", Set.of("ALL")),
			Map.entry("/api/company/search GET", Set.of("ALL")),
			Map.entry("/api/company/{id} PATCH", Set.of("HUB", "MASTER", "VEN_OFFICER")),
			Map.entry("/api/company/{id} DELETE", Set.of("HUB", "MASTER"))
		);

		return roleRequirements.entrySet().stream()
			.anyMatch(e -> path.matches(e.getKey().split(" ")[0] + ".*") && method.equals(e.getKey().split(" ")[1]) && (
				e.getValue().contains(role) || e.getValue().contains("ALL")));
	}
}
