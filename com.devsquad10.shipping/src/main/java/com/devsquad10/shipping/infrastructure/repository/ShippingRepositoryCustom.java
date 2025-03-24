package com.devsquad10.shipping.infrastructure.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

import com.devsquad10.shipping.application.dto.request.ShippingSearchReqDto;
import com.devsquad10.shipping.domain.model.Shipping;

public interface ShippingRepositoryCustom {
	Page<Shipping> findAll(@Param("request") ShippingSearchReqDto request);
}
