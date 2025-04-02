package com.devsquad10.shipping.infrastructure.client.dto;

import java.util.UUID;

import com.devsquad10.shipping.domain.enums.ShippingAgentType;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ShippingAgentFeignClientPostRequest {

	// 유저:기본키(id) = 배송담당자 ID(shipping_manager_id)
	private UUID shippingManagerId;
	private String slackId;
	private UUID hubId;
	@JsonProperty("type")
	private ShippingAgentType type;
}
