package com.devsquad10.hub.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.devsquad10.hub.domain.model.HubRouteWaypoint;
import com.devsquad10.hub.infrastructure.repository.JpaHubRouteWaypointRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class HubRouteWaypointRepositoryImpl implements HubRouteWaypointRepository {

	private final JpaHubRouteWaypointRepository jpaHubRouteWaypointRepository;

	@Override
	public HubRouteWaypoint save(HubRouteWaypoint waypoint) {
		return jpaHubRouteWaypointRepository.save(waypoint);
	}

	@Override
	public List<HubRouteWaypoint> findByHubRouteIdOrderBySequence(UUID hubRouteId) {
		return jpaHubRouteWaypointRepository.findByHubRouteIdOrderBySequence(hubRouteId);
	}

}
