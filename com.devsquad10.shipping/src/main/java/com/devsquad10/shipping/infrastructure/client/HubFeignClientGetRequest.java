package com.devsquad10.shipping.infrastructure.client;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HubFeignClientGetRequest {

	// 유저:기본키(id) = 배송담당자 ID(shipping_manager_id)
	// Ex) 경유지(허브1->허브3->허브4) 중, 허브1->허브3는 순번1
	private Integer sequence; // 1 허브 경유지 순번
	private UUID departureHubId; // 허브1 ID
	private UUID destinationHubId; // 허브3 ID
	private Integer time;
	private Double distance;
}

