package com.devsquad10.user.application.service;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.user.application.dto.ShippingAgentFeignClientPatchRequest;
import com.devsquad10.user.application.dto.ShippingAgentFeignClientPostRequest;
import com.devsquad10.user.application.dto.UserInfoFeignClientResponse;
import com.devsquad10.user.application.dto.UserLoginRequestDto;
import com.devsquad10.user.application.dto.UserRequestDto;
import com.devsquad10.user.application.dto.UserResponseDto;
import com.devsquad10.user.domain.model.User;
import com.devsquad10.user.domain.model.UserRoleEnum;
import com.devsquad10.user.domain.repository.UserRepository;
import com.devsquad10.user.infrastructure.client.ShippingClient;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ShippingClient shippingClient;
	public static final String AUTHORIZATION_HEADER = "Authorization";
	private final String BEARER_PREFIX = "Bearer ";

	@Value("${service.jwt.master-key}")
	private String MASTER_KEY;

	@Value("${spring.application.name}")
	private String issuer;

	@Value("${service.jwt.access-expiration}")
	private Long accessExpiration;

	@Value("${service.jwt.secret-key}")
	private String secretKey;

	public void signup(UserRequestDto requestDto) {
		duplicationCheck(requestDto.getUsername(), requestDto.getEmail());

		if (requestDto.getRole().equals(UserRoleEnum.MASTER)) {
			checkMasterKey(requestDto.getMasterKey());
		}

		String password = passwordEncoder.encode(requestDto.getPassword());
		User user = userRepository.save(new User(requestDto, password));

		if (user.getRole() == UserRoleEnum.DVL_OFFICER) {
			ShippingAgentFeignClientPostRequest shippingRequest = new ShippingAgentFeignClientPostRequest();
			shippingRequest.setId(user.getId());
			shippingRequest.setSlackId(user.getSlackId());

			shippingClient.createShippingAgent(shippingRequest);
		}
	}

	private void duplicationCheck(String username, String email) {
		userRepository.findByUsername(username).ifPresent((m) -> {
			throw new IllegalArgumentException("이미 존재하는 사용자 이름입니다.");
		});
	}

	private void checkMasterKey(String masterKey) {
		if (!masterKey.equals(MASTER_KEY)) {
			throw new IllegalArgumentException("관리자 키가 일치하지 않습니다.");
		}
	}

	public String signIn(UserLoginRequestDto requestDto) {
		User user = (User)userRepository.findByUsername(requestDto.getUsername())
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다."));

		if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}

		return createAccessToken(user);
	}

	@Transactional(readOnly = true)
	public UserResponseDto getUserInfo(UUID id) {
		log.info("유저 정보 조회 서비스 실행");
		User user = (User)userRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다."));
		log.info("유저 정보 조회 서비스 실행 완료");
		return new UserResponseDto(user);
	}

	@Transactional(readOnly = true)
	public Page<UserResponseDto> searchUser(String query, UserRoleEnum userRoleEnum, int page, int size, String sort,
		String order) {
		// 🔹 정렬 방향 설정
		Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

		// 🔹 Repository 호출 (QueryDSL 사용)
		return userRepository.findByUsernameContainingAndRole(query, userRoleEnum, pageable)
			.map(UserResponseDto::new);
	}

	@Transactional
	public void updateUserInfo(UUID id, UserRequestDto requestDto) {
		User user = (User)userRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다."));

		if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
			throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
		}
		if (userRepository.findBySlackId(requestDto.getSlackId()).isPresent()) {
			throw new IllegalArgumentException("이미 존재하는 슬랙 ID입니다.");
		}
		if (user.getRole() == UserRoleEnum.DVL_OFFICER) {
			ShippingAgentFeignClientPatchRequest shippingRequest = new ShippingAgentFeignClientPatchRequest(
				user.getId(), requestDto.getSlackId());

			shippingClient.infoUpdateShippingAgent(shippingRequest);
		}

		user.update(requestDto);
	}

	public void deleteUser(UUID id) {
		User user = (User)userRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다."));

		user.delete(id);
		userRepository.save(user);
	}

	public String createAccessToken(User user) {
		SecretKey deSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));

		return BEARER_PREFIX + Jwts.builder()
			// 사용자 ID를 클레임으로 설정
			.subject(user.getId().toString())
			.claim("slack_id", user.getSlackId())
			.claim("role", user.getRole())// JWT 발행자를 설정
			.issuer(issuer)// JWT 발행 시간을 현재 시간으로 설정.
			.issuedAt(new Date(System.currentTimeMillis()))// JWT 만료 시간을 설정
			.expiration(new Date(System.currentTimeMillis() + accessExpiration))// SecretKey를 사용하여 HMAC-SHA512 알고리즘으로 서명
			.signWith(deSecretKey, io.jsonwebtoken.SignatureAlgorithm.HS512)// JWT 문자열로 컴팩트하게 변환
			.compact();
	}

	public void addJwtToHeader(String token, HttpServletResponse res) {
		res.setHeader(AUTHORIZATION_HEADER, token);
	}

	public UserInfoFeignClientResponse getUserInfoRequest(UUID id) {
		log.info("유저 정보 조회");
		User user = (User)userRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new IllegalArgumentException("가입되지 않은 사용자입니다."));
		log.info("유저 정보 조회 완료");
		UserInfoFeignClientResponse userInfo = new UserInfoFeignClientResponse(user.getUsername(), user.getSlackId());
		return userInfo;
	}
}
