package com.devsquad10.shipping.infrastructure.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoFeignClientRequest {
	private String username;
	private String slackId;
}
