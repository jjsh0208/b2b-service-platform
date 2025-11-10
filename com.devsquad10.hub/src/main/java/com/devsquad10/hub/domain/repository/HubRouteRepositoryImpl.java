package com.devsquad10.hub.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import com.devsquad10.hub.application.dto.req.HubRouteSearchRequestDto;
import com.devsquad10.hub.domain.model.Hub;
import com.devsquad10.hub.domain.model.HubRoute;
import com.devsquad10.hub.infrastructure.repository.JpaHubRouteRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class HubRouteRepositoryImpl implements HubRouteRepository {

	private final JpaHubRouteRepository jpaHubRouteRepository;

	@Override
	public HubRoute save(HubRoute hubRoute) {
		return jpaHubRouteRepository.save(hubRoute);
	}

	@Override
	public Optional<HubRoute> findById(UUID id) {
		return jpaHubRouteRepository.findById(id);
	}

	@Override
	public Page<HubRoute> findAll(HubRouteSearchRequestDto request) {
		return jpaHubRouteRepository.findAll(request);
	}

	@Override
	public Optional<HubRoute> findByDepartureHubAndDestinationHub(Hub departureHub, Hub destinationHub) {
		return jpaHubRouteRepository.findByDepartureHubAndDestinationHub(departureHub, destinationHub);
	}

	@Override
	public Optional<HubRoute> findByIdWithWaypoints(UUID id) {
		return jpaHubRouteRepository.findByIdWithWaypoints(id);
	}
}
