package com.devsquad10.message.application.dto.res;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.devsquad10.message.domain.model.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메시지 목록의 개별 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedMessageItemResponseDto implements Serializable {

	private UUID id;
	private String message;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static PagedMessageItemResponseDto toResponseDto(Message message) {
		return PagedMessageItemResponseDto.builder()
			.id(message.getId())
			.message(message.getMessage())
			.createdAt(message.getCreatedAt())
			.updatedAt(message.getUpdatedAt())
			.build();
	}
}
