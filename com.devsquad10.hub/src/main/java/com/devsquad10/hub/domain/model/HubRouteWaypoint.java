package com.devsquad10.hub.domain.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 경유지
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "p_hub_route_waypoint")
public class HubRouteWaypoint extends Base {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hub_route_id", nullable = false)
	private HubRoute hubRoute;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "departure_hub_id", nullable = false)
	private Hub departureHub;

	// 도착 허브
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "destination_hub_id", nullable = false)
	private Hub destinationHub;

	// 경유지 순서
	private Integer sequence;

	private Double distance;

	private Integer duration;
}
