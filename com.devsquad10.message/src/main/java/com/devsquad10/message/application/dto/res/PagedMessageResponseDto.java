package com.devsquad10.message.application.dto.res;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;

import com.devsquad10.message.application.dto.enums.MessageSortOption;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PagedMessageResponseDto implements Serializable {
	private List<PagedMessageItemResponseDto> messages;
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
	private MessageSortOption sortOption;

	public static PagedMessageResponseDto toResponseDto(
		Page<PagedMessageItemResponseDto> page,
		MessageSortOption sortOption
	) {
		return PagedMessageResponseDto.builder()
			.messages(page.getContent())
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
