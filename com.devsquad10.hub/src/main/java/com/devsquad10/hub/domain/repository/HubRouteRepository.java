package com.devsquad10.hub.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.devsquad10.hub.application.dto.req.HubRouteSearchRequestDto;
import com.devsquad10.hub.domain.model.Hub;
import com.devsquad10.hub.domain.model.HubRoute;

public interface HubRouteRepository {
	HubRoute save(HubRoute hubRoute);

	Optional<HubRoute> findById(UUID id);

	Page<HubRoute> findAll(HubRouteSearchRequestDto request);

	Optional<HubRoute> findByDepartureHubAndDestinationHub(Hub departureHub, Hub destinationHub);

	Optional<HubRoute> findByIdWithWaypoints(UUID id);
}
