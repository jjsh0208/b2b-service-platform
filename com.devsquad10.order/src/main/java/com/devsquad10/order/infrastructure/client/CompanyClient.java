package com.devsquad10.order.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "${feign.client.company.name}", url = "${feign.client.company.url}")
public interface CompanyClient {

	@GetMapping("/address/{id}")
	String findRecipientAddressByCompanyId(@PathVariable("id") UUID id);
}
