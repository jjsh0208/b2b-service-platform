package com.devsquad10.company.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "${feign.client.hub.name}", url = "${feign.client.hub.url}")
public interface HubClient {

	@GetMapping("/exists/{uuid}")
	Boolean isHubExists(@PathVariable("uuid") UUID uuid);
}
