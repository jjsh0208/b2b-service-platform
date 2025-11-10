package com.devsquad10.user.infrastructure.config;

import java.util.List;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import lombok.RequiredArgsConstructor;

@Configuration // Spring Configuration 클래스를 나타냅니다.
@EnableWebSecurity // Spring Security를 활성화합니다.
@RequiredArgsConstructor // Lombok 어노테이션으로, final 필드나 @NonNull 필드에 대해 생성자를 자동으로 생성합니다.
public class AuthConfig {

	private static final String GET = HttpMethod.GET.name();
	private static final String POST = HttpMethod.POST.name();

	// 비밀번호 인코더 빈을 생성합니다.
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// AuthenticationManager 빈을 생성합니다.
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
		throws Exception {
		return configuration.getAuthenticationManager();
	}

	// 보안 필터 체인을 설정합니다.
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf((csrf) -> csrf.disable()); // CSRF 보호를 비활성화합니다.

		http.sessionManagement((sessionManagement) ->
			sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 상태없음(Stateless)으로 설정합니다.
		);

		http.authorizeHttpRequests((authorizeHttpRequests) ->
			authorizeHttpRequests
				.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
				.permitAll() // 정적 리소스에 대한 요청을 허용합니다.
				.requestMatchers(publicEndPoints())
				.permitAll() // 특정 엔드포인트에 대한 요청을 허용합니다.
				.anyRequest()
				.authenticated() // 나머지 요청은 인증을 요구합니다.
		);

		return http.build();
	}

	// 공개 엔드포인트를 정의합니다.
	private RequestMatcher publicEndPoints() {
		List<RequestMatcher> matchers = List.of(
			new AntPathRequestMatcher("/api/user/**")
		);
		return new OrRequestMatcher(matchers);
	}
}
