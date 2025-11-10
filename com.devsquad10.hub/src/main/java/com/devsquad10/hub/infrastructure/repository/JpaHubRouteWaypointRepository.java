package com.devsquad10.hub.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devsquad10.hub.domain.model.HubRouteWaypoint;

@Repository
public interface JpaHubRouteWaypointRepository extends JpaRepository<HubRouteWaypoint, UUID> {
	List<HubRouteWaypoint> findByHubRouteIdOrderBySequence(UUID hubRouteId);
}
