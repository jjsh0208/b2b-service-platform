package com.devsquad10.hub.infrastructure.repository;

import org.springframework.data.domain.Page;

import com.devsquad10.hub.application.dto.req.HubSearchRequestDto;
import com.devsquad10.hub.domain.model.Hub;

public interface HubRepositoryCustom {
	Page<Hub> findAll(HubSearchRequestDto request);
}
