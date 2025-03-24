package com.devsquad10.message.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.devsquad10.message.infrastructure.client.dto.ShippingClientData;

@FeignClient(name = "${feign.client.shipping.name}", url = "${feign.client.shipping.url}")
public interface ShippingClient {
	// 데이터 검증
	@GetMapping("/exists/{uuid}")
	Boolean isShippingDataExists(@PathVariable(name = "uuid") UUID uuid);

	@GetMapping("/delivery-notification-data/{orderId}")
	ShippingClientData getShippingClientData(@PathVariable("orderId") UUID orderId);
}
