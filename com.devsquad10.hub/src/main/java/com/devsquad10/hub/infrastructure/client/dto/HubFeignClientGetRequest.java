package com.devsquad10.hub.infrastructure.client.dto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.devsquad10.hub.domain.model.HubRoute;
import com.devsquad10.hub.domain.model.HubRouteWaypoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubFeignClientGetRequest {

	// 유저:기본키(id) = 배송담당자 ID(shipping_manager_id)
	// Ex) 경유지(허브1->허브3->허브4) 중, 허브1->허브3는 순번1
	private Integer sequence; // 1 허브 경유지 순번
	private UUID departureHubId; // 허브1 ID
	private UUID destinationHubId; // 허브3 ID
	private Integer time;
	private Double distance;

	public static List<HubFeignClientGetRequest> from(HubRoute hubRoute) {

		List<HubFeignClientGetRequest> result = new ArrayList<>();

		List<HubRouteWaypoint> waypoints = hubRoute.getWaypoints().stream()
			.sorted(Comparator.comparing(HubRouteWaypoint::getSequence))
			.toList();

		for (HubRouteWaypoint waypoint : waypoints) {
			result.add(
				HubFeignClientGetRequest.builder()
					.sequence(waypoint.getSequence())
					.departureHubId(waypoint.getDepartureHub().getId())
					.destinationHubId(waypoint.getDestinationHub().getId())
					.distance(waypoint.getDistance())
					.time(waypoint.getDuration())
					.build()
			);
		}

		return result;
	}
}
