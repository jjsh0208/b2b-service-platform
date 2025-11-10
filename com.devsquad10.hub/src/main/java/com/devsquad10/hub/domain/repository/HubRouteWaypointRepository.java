package com.devsquad10.hub.domain.repository;

import java.util.List;
import java.util.UUID;

import com.devsquad10.hub.domain.model.HubRouteWaypoint;

public interface HubRouteWaypointRepository {
	HubRouteWaypoint save(HubRouteWaypoint waypoint);

	List<HubRouteWaypoint> findByHubRouteIdOrderBySequence(UUID hubRouteId);
}
