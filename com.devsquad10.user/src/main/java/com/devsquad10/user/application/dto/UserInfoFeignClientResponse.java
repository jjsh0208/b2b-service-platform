package com.devsquad10.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Data
@AllArgsConstructor
public class UserInfoFeignClientResponse {
	private String username;
	private String slackId;
}
