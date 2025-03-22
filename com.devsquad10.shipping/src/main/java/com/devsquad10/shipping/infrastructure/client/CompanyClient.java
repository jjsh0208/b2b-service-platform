package com.devsquad10.shipping.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "company", url = "http://localhost:19093/api/company")
public interface CompanyClient {
	@GetMapping("/info/{id}")
	ShippingCompanyInfoDto findShippingCompanyInfo(@PathVariable("id") UUID id);
}