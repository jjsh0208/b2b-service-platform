package com.devsquad10.hub.application.dto.req;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.devsquad10.hub.application.dto.enums.HubRouteSortOption;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HubRouteSearchRequestDto {
	private UUID id;
	private UUID departureHubId;
	private UUID destinationHubId;
	private String departureHubName;
	private String destinationHubName;

	@PositiveOrZero
	private Double minDistance;

	@PositiveOrZero
	private Double maxDistance;

	@PositiveOrZero
	private Integer minDuration;

	@PositiveOrZero
	private Integer maxDuration;

	@Builder.Default
	private Integer size = 10;

	@Builder.Default
	private Integer page = 0;

	@Builder.Default
	private HubRouteSortOption sortOption = HubRouteSortOption.CREATED_AT;

	@Builder.Default
	private Sort.Direction sortOrder = Sort.Direction.DESC;

	public int getPage() {
		return (page != null && page > 0) ? page - 1 : 0;
	}

	public int getSize() {
		return Optional.ofNullable(size)
			.filter(s -> Set.of(10, 30, 50).contains(s))
			.orElse(10);
	}
}
