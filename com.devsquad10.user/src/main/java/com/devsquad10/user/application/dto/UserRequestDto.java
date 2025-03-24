package com.devsquad10.user.application.dto;

import java.util.UUID;

import com.devsquad10.user.domain.model.ShippingAgentType;
import com.devsquad10.user.domain.model.UserRoleEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserRequestDto {

	@Size(min = 4, max = 10, message = "아이디는 4자 이상, 10자 이하여야 합니다.")
	@Pattern(regexp = "^[a-z0-9]+$", message = "아이디는 소문자(a-z)와 숫자(0-9)만 포함할 수 있습니다.")
	private String username;

	@Size(min = 8, max = 15, message = "비밀번호는 8자 이상, 15자 이하여야 합니다.")
	@Pattern(
		regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,15}$",
		message = "비밀번호는 영문 대소문자, 숫자, 특수문자를 포함해야 합니다."
	)
	private String password;

	@Email
	private String email;

	private String slackId;
	private UserRoleEnum role;
	private String masterKey;
	private UUID hubId;
	private ShippingAgentType type;
}
