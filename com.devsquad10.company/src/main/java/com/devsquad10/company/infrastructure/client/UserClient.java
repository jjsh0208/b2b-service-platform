package com.devsquad10.company.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "${feign.client.user.name}", url = "${feign.client.user.url}")
public interface UserClient {

	@GetMapping("/slackId/{userId}")
	String getUserSlackId(@PathVariable("userId") UUID userId);

}
