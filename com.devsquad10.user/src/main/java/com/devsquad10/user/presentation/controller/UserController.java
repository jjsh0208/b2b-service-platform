package com.devsquad10.user.presentation.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devsquad10.user.application.dto.UserInfoFeignClientResponse;
import com.devsquad10.user.application.dto.UserLoginRequestDto;
import com.devsquad10.user.application.dto.UserRequestDto;
import com.devsquad10.user.application.dto.UserResponseDto;
import com.devsquad10.user.application.service.UserService;
import com.devsquad10.user.domain.model.UserRoleEnum;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	private final UserService userService;

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody UserRequestDto requestDto) {
		userService.signup(requestDto);
		return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
	}

	@PostMapping("/signIn")
	public ResponseEntity<?> signIn(@RequestBody UserLoginRequestDto requestDto, HttpServletResponse res) {
		String token = userService.signIn(requestDto);
		userService.addJwtToHeader(token, res);

		return ResponseEntity.status(HttpStatus.OK)
			.body("로그인 성공");
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getUserInfo(@PathVariable UUID id) {
		log.info("유저 정보 조회");
		UserResponseDto userInfo = userService.getUserInfo(id);
		if (userInfo == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body("유저를 찾을 수 없습니다.");
		}
		return ResponseEntity.status(HttpStatus.OK)
			.body(userInfo);
	}

	@GetMapping("/search")
	public ResponseEntity<?> searchUser(@RequestParam(required = false) String q,
		@RequestParam(required = false) UserRoleEnum userRole,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String order) {
		Page<UserResponseDto> userInfo = userService.searchUser(q, userRole, page - 1, size, sort, order);
		return ResponseEntity.status(HttpStatus.OK)
			.body(userInfo);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<?> updateUserInfo(@PathVariable UUID id, @RequestBody UserRequestDto requestDto) {
		userService.updateUserInfo(id, requestDto);
		return ResponseEntity.status(HttpStatus.OK)
			.body("유저 정보 수정 완료");
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
		userService.deleteUser(id);
		return ResponseEntity.status(HttpStatus.OK)
			.body("유저 삭제 완료");
	}

	@GetMapping("/info/{id}")
	UserInfoFeignClientResponse getUserInfoRequest(@PathVariable("id") UUID id) {
		log.info("유저 정보 조회");
		UserInfoFeignClientResponse userInfo = userService.getUserInfoRequest(id);
		if (userInfo == null) {
			return null;
		}
		return userInfo;
	}
}
