package com.devsquad10.hub.application.dto.req;

import java.util.UUID;

import com.devsquad10.hub.application.dto.enums.HubRouteStrategyType;

import jakarta.validation.constraints.NotNull;
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
public class HubRouteCreateRequestDto {
	@NotNull
	private UUID departureHubId;

	@NotNull
	private UUID destinationHubId;

	@Builder.Default
	private HubRouteStrategyType strategyType = HubRouteStrategyType.HUB_TO_HUB_RELAY;
}
