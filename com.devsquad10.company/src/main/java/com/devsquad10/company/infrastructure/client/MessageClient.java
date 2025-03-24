package com.devsquad10.company.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.devsquad10.company.application.dto.SoldOutMessageRequest;

@FeignClient(name = "${feign.client.message.name}", url = "${feign.client.message.url}")
public interface MessageClient {
	@PostMapping("stock-depletion")
	void sendSoldOutMessage(@RequestBody SoldOutMessageRequest request);
}
