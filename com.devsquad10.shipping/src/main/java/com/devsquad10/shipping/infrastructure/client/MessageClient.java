package com.devsquad10.shipping.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.devsquad10.shipping.infrastructure.client.dto.ShippingClientDataResponseDto;

@FeignClient(name = "message", url = "http://localhost:19095/api/message")
public interface MessageClient {

	@GetMapping("/shipping-time/{orderId}")
	ShippingClientDataResponseDto getShippingClientData(@PathVariable(name = "orderId") UUID orderId);
}
