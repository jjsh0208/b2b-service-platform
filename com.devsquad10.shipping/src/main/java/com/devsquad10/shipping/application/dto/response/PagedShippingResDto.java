package com.devsquad10.shipping.application.dto.response;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;

import com.devsquad10.shipping.application.dto.enums.ShippingSortOption;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Pagination
 * Shipping(배송) 전체 목록 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedShippingResDto implements Serializable {
	private List<PagedShippingItemResDto> shippings;
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
	private ShippingSortOption sortOption;

	public static PagedShippingResDto toResponseDto(
		Page<PagedShippingItemResDto> page,
		ShippingSortOption sortOption
	) {
		return PagedShippingResDto.builder()
			.shippings(page.getContent())
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
