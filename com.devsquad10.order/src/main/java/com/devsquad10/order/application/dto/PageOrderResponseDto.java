package com.devsquad10.order.application.dto;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PageOrderResponseDto implements Serializable {
	private List<OrderResDto> content;
	private int totalPages;
	private long totalElements;
	private int pageSize;
	private int currentPage;

	public static PageOrderResponseDto toResponse(Page<OrderResDto> page) {
		return PageOrderResponseDto.builder()
			.content(page.getContent())
			.totalPages(page.getTotalPages())
			.totalElements(page.getTotalElements())
			.pageSize(page.getSize())
			.currentPage(page.getNumber() + 1)
			.build();
	}
}
