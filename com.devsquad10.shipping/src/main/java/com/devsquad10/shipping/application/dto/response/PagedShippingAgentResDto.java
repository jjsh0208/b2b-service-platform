package com.devsquad10.shipping.application.dto.response;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;

import com.devsquad10.shipping.application.dto.enums.ShippingAgentSortOption;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Pagination
 * ShippingAgent(배송담당자) 전체 목록 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedShippingAgentResDto implements Serializable {
	private List<PagedShippingAgentItemResDto> shippingAgents;
	private int totalPages;
	private long totalElements;
	private int pageSize;
	private int currentPage;

	@JsonProperty("first")
	private boolean isFirst;

	@JsonProperty("last")
	private boolean isLast;

	private boolean hasNext;
	private boolean hasPrevious;
	private ShippingAgentSortOption sortOption;

	public static PagedShippingAgentResDto toResponseDto(
		Page<PagedShippingAgentItemResDto> page,
		ShippingAgentSortOption sortOption
	) {
		return PagedShippingAgentResDto.builder()
			.shippingAgents(page.getContent())
			.totalPages(page.getTotalPages())
			.totalElements(page.getTotalElements())
			.pageSize(page.getSize())
			.currentPage(page.getNumber() + 1)
			.isFirst(page.isFirst())
			.isLast(page.isLast())
			.hasNext(page.hasNext())
			.hasPrevious(page.hasPrevious())
			.sortOption(sortOption)
			.build();
	}
}
