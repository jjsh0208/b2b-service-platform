package com.devsquad10.shipping.application.dto.response;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;

import com.devsquad10.shipping.application.dto.enums.ShippingHistorySortOption;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Pagination
 * ShippingHistory(배송경로기록) 전체 목록 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedShippingHistoryResDto implements Serializable {
	private List<PagedShippingHistoryItemResDto> shippingHistories;
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
	private ShippingHistorySortOption sortOption;

	public static PagedShippingHistoryResDto toResponseDto(
		Page<PagedShippingHistoryItemResDto> page,
		ShippingHistorySortOption sortOption
	) {
		return PagedShippingHistoryResDto.builder()
			.shippingHistories(page.getContent())
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
