package com.devsquad10.company.application.dto;

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
public class PageCompanyResponseDto implements Serializable {
	private List<CompanyResDto> content;
	private int totalPages;
	private long totalElements;
	private int pageSize;
	private int currentPage;

	public static PageCompanyResponseDto toResponse(Page<CompanyResDto> page) {
		return PageCompanyResponseDto.builder()
			.content(page.getContent())
			.totalPages(page.getTotalPages())
			.totalElements(page.getTotalElements())
			.pageSize(page.getSize())
			.currentPage(page.getNumber() + 1)
			.build();
	}
}
