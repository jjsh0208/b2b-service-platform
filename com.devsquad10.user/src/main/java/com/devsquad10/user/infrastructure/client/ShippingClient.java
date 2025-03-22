package com.devsquad10.user.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.devsquad10.user.application.dto.ShippingAgentFeignClientPatchRequest;
import com.devsquad10.user.application.dto.ShippingAgentFeignClientPostRequest;

@FeignClient(name = "shippingAgent", url = "http://localhost:19098/api/shipping-agent")
public interface ShippingClient {

	@PostMapping
	void createShippingAgent(@RequestBody ShippingAgentFeignClientPostRequest request);

	@PatchMapping("/info-update")
	void infoUpdateShippingAgent(
		@RequestBody ShippingAgentFeignClientPatchRequest request);
}