package com.devsquad10.user.application.dto;

import com.devsquad10.user.domain.model.User;
import com.devsquad10.user.domain.model.UserRoleEnum;

import lombok.Getter;

@Getter
public class UserResponseDto {
	private String username;
	private String email;
	private String slackId;
	private UserRoleEnum role;

	public UserResponseDto(User user) {
		this.username = user.getUsername();
		this.email = user.getEmail();
		this.slackId = user.getSlackId();
		this.role = user.getRole();
	}
}
