package com.devsquad10.user.application.dto;

import java.util.UUID;

public class ShippingAgentFeignClientPatchRequest {
	// 유저:기본키(id) = 배송담당자 ID(shipping_manager_id)
	private UUID shippingManagerId;
	private String slackId;

	public ShippingAgentFeignClientPatchRequest(UUID shippingManagerId, String slackId) {
		this.shippingManagerId = shippingManagerId;
		this.slackId = slackId;
	}
}
