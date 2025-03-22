package com.devsquad10.hub.application.dto.res;

import java.util.UUID;

import com.devsquad10.hub.domain.model.HubRouteWaypoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubRouteWaypointDto {
	private UUID id;
	private Integer sequence;
	private UUID departureHubId;
	private String departureHubName;
	private UUID destinationHubId;
	private String destinationHubName;
	private Double distance;
	private Integer duration;

	public static HubRouteWaypointDto fromEntity(HubRouteWaypoint waypoint) {
		return HubRouteWaypointDto.builder()
			.id(waypoint.getId())
			.sequence(waypoint.getSequence())
			.departureHubId(waypoint.getDepartureHub().getId())
			.departureHubName(waypoint.getDepartureHub().getName())
			.destinationHubId(waypoint.getDestinationHub().getId())
			.destinationHubName(waypoint.getDestinationHub().getName())
			.distance(waypoint.getDistance())
			.duration(waypoint.getDuration())
			.build();
	}
}
