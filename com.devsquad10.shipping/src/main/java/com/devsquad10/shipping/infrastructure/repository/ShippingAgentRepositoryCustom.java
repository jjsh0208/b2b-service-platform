package com.devsquad10.shipping.infrastructure.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

import com.devsquad10.shipping.application.dto.request.ShippingAgentSearchReqDto;
import com.devsquad10.shipping.domain.model.ShippingAgent;

public interface ShippingAgentRepositoryCustom {
	Page<ShippingAgent> findAll(@Param("request") ShippingAgentSearchReqDto request);
}
