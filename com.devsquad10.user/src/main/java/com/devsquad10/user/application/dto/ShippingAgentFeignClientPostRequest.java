package com.devsquad10.user.application.dto;

import java.util.UUID;

import com.devsquad10.user.domain.model.ShippingAgentType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingAgentFeignClientPostRequest {

	private UUID shippingManagerId;        // 유저 ID -> 배송담당자 ID로 사용
	private String slackId;
	private UUID hubId;
	private ShippingAgentType type;// Slack ID
}
