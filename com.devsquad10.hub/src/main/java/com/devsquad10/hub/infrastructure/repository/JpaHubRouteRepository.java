package com.devsquad10.hub.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.devsquad10.hub.domain.model.Hub;
import com.devsquad10.hub.domain.model.HubRoute;

@Repository
public interface JpaHubRouteRepository extends JpaRepository<HubRoute, UUID>, HubRouteRepositoryCustom {
	Optional<HubRoute> findByDepartureHubAndDestinationHub(Hub departureHub, Hub destinationHub);

	@Query("SELECT hr FROM HubRoute hr LEFT JOIN FETCH hr.waypoints w LEFT JOIN FETCH w.departureHub LEFT JOIN FETCH w.destinationHub WHERE hr.id = :id")
	Optional<HubRoute> findByIdWithWaypoints(UUID id);
}
