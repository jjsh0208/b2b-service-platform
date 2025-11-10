package com.devsquad10.hub.application.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubRouteUpdateRequestDto {
	@NotNull
	private Double distance;

	@NotNull
	private Integer duration;
}
