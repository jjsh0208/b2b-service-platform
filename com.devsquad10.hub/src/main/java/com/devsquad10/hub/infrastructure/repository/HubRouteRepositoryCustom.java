package com.devsquad10.hub.infrastructure.repository;

import org.springframework.data.domain.Page;

import com.devsquad10.hub.application.dto.req.HubRouteSearchRequestDto;
import com.devsquad10.hub.domain.model.HubRoute;

public interface HubRouteRepositoryCustom {
	Page<HubRoute> findAll(HubRouteSearchRequestDto request);
}
