package com.devsquad10.order.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "${feign.client.shipping.name}", url = "${feign.client.shipping.url}")
public interface ShippingClient {

	@DeleteMapping("/order/{orderId}")
	boolean deleteShippingForOrder(@PathVariable UUID orderId);
}
