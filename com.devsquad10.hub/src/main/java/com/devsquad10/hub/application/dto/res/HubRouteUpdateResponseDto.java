package com.devsquad10.hub.application.dto.res;

import java.time.LocalDateTime;
import java.util.UUID;

import com.devsquad10.hub.domain.model.HubRoute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class HubRouteUpdateResponseDto {
	private UUID id;
	private UUID departureHubId;
	private String departureHubName;
	private UUID destinationHubId;
	private String destinationHubName;
	private Double distance;
	private Integer duration;
	private LocalDateTime updatedAt;
	private UUID updatedBy;

	public static HubRouteUpdateResponseDto toResponseDto(HubRoute hubRoute) {
		return HubRouteUpdateResponseDto.builder()
			.id(hubRoute.getId())
			.departureHubId(hubRoute.getDepartureHub().getId())
			.departureHubName(hubRoute.getDepartureHub().getName())
			.destinationHubId(hubRoute.getDestinationHub().getId())
			.destinationHubName(hubRoute.getDestinationHub().getName())
			.distance(hubRoute.getDistance())
			.duration(hubRoute.getDuration())
			.updatedAt(hubRoute.getUpdatedAt())
			.updatedBy(hubRoute.getUpdatedBy())
			.build();
	}
}
