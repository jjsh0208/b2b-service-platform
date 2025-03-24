package com.devsquad10.product.application.dto;

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
public class PageProductResponseDto implements Serializable {
	private List<ProductResDto> content;
	private int totalPages;
	private long totalElements;
	private int pageSize;
	private int currentPage;

	public static PageProductResponseDto toResponse(Page<ProductResDto> page) {
		return PageProductResponseDto.builder()
			.content(page.getContent())
			.totalPages(page.getTotalPages())
			.totalElements(page.getTotalElements())
			.pageSize(page.getSize())
			.currentPage(page.getNumber() + 1)
			.build();
	}
}
