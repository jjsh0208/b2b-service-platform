package com.devsquad10.shipping.application.dto.request;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.devsquad10.shipping.application.dto.enums.ShippingAgentSortOption;

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
public class ShippingAgentSearchReqDto {
	private UUID id;

	private UUID hubId;

	private UUID shippingManagerId;

	@Builder.Default
	private Integer page = 0;

	@Builder.Default
	private Integer size = 10;

	@Builder.Default
	private ShippingAgentSortOption sortOption = ShippingAgentSortOption.CREATED_AT;

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
