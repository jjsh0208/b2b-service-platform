package com.devsquad10.shipping.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserInfoFeignClientRequest {
	private String username;
	private String slackId;

	public UserInfoFeignClientRequest toRequest() {
		return UserInfoFeignClientRequest.builder()
			.username(this.username)
			.slackId(this.slackId)
			.build();
	}
}
