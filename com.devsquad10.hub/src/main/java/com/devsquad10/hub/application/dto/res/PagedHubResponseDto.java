package com.devsquad10.hub.application.dto.res;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;

import com.devsquad10.hub.application.dto.enums.HubSortOption;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PagedHubResponseDto implements Serializable {
	private List<PagedHubItemResponseDto> hubs;
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
	private HubSortOption sortOption;

	public static PagedHubResponseDto toResponseDto(
		Page<PagedHubItemResponseDto> page,
		HubSortOption sortOption
	) {
		return PagedHubResponseDto.builder()
			.hubs(page.getContent())
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
