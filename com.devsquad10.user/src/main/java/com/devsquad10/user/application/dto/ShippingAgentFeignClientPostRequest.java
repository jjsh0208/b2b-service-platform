package com.devsquad10.user.application.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingAgentFeignClientPostRequest {

	private UUID id;        // 유저 ID -> 배송담당자 ID로 사용
	private String slackId; // Slack ID
}
