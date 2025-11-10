package com.devsquad10.hub.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 허브 이동 경로
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "p_hub_route")
public class HubRoute extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private Double distance;

	private Integer duration;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "departure_hub_id", nullable = false)
	private Hub departureHub;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "destination_hub_id", nullable = false)
	private Hub destinationHub;

	@OneToMany(mappedBy = "hubRoute", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<HubRouteWaypoint> waypoints = new ArrayList<>();

	public void update(Double distance, Integer duration) {
		this.distance = distance;
		this.duration = duration;
	}
}

