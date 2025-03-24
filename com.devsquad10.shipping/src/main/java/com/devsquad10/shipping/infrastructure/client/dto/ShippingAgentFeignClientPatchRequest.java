package com.devsquad10.shipping.infrastructure.client.dto;

import java.util.UUID;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingAgentFeignClientPatchRequest {

	// 유저:기본키(id) = 배송담당자 ID(shipping_manager_id)
	private UUID shippingManagerId;
	private String slackId;
	private UUID hubId;
}
