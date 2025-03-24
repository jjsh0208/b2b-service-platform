package com.devsquad10.user.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.devsquad10.user.application.dto.ShippingAgentFeignClientPatchRequest;
import com.devsquad10.user.application.dto.ShippingAgentFeignClientPostRequest;

// ShippingAgentClient 수정해주세요!!
// true 인 경우, 배송담당자 등록 성공
// User 디렉토리 infrastructure > client 에 생성하여 userService에서 사용해주세요.
@FeignClient(name = "shippingAgent", url = "http://localhost:19098/api/shipping-agent")
public interface ShippingClient {
	@PostMapping
	boolean createShippingAgent(@RequestBody ShippingAgentFeignClientPostRequest request);

	@PatchMapping("/info-update")
	boolean infoUpdateShippingAgent(@RequestBody ShippingAgentFeignClientPatchRequest request);

	@DeleteMapping("/user/{shippingManagerId}")
	boolean deleteShippingAgentForUser(@PathVariable(name = "shippingManagerId") UUID shippingManagerId);
}
