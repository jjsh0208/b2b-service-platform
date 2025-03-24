package com.devsquad10.user.application.dto;

import lombok.Getter;

@Getter
public class UserInfoFeignClientResponse {
	private String username;
	private String slackId;

	public UserInfoFeignClientResponse(String username, String slackId) {
		this.username = username;
		this.slackId = slackId;
	}
}
