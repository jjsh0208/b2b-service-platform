package com.devsquad10.shipping.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.devsquad10.shipping.infrastructure.client.dto.OrderFeignClientDto;

@FeignClient(name = "order", url = "http://order:8080/api/order")
public interface OrderClient{

	@GetMapping("/products/{id}")
	OrderFeignClientDto getOrderProductDetails(@PathVariable(name = "id") UUID id);

	@PatchMapping("/shipping/{shippingId}")
	void updateOrderStatusToShipped(@PathVariable("shippingId") UUID shippingId);
}