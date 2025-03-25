package com.devsquad10.shipping.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.devsquad10.shipping.infrastructure.client.dto.UserInfoFeignClientResponse;

@FeignClient(name = "user", url = "http://localhost:19099/api/user")
public interface UserClient {
	@GetMapping("/info/{id}")
	UserInfoFeignClientResponse getUserInfoRequest(@PathVariable("id") UUID id);
}
