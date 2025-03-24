package com.devsquad10.shipping.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingClientDataRequestDto {
	// 주문 ID
	private UUID orderId;
	// 주문자 이름
	private String customerName;
	// 상품 이름
	private String productInfo;
	// 상품 개수
	private Integer quantity;
	// 주문 요청사항
	private String requestDetails;
	// 허브 출발지
	private String departureHubName;
	// 경로 경유지
	private List<String> waypointHubNames;
	// 허브 도착지
	private String destinationHubName;
	// 배송지 주소
	private String address;
	// 배송 담당자
	private String shippingManagerName;
}
