package com.devsquad10.order.application.dto.message;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingResponseMessage {

	private UUID orderId; // 주문 ID

	private UUID shippingId; // 배송 ID

	private String status; // SUCCESS , FAIL
}
