package com.devsquad10.shipping.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user", url = "http://localhost:19099/api/user")
public interface UserClient {
	@GetMapping("/info/{id}")
	UserInfoFeignClientRequest getUserInfoRequest(@PathVariable("id") UUID id);
}
