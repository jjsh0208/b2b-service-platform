package com.devsquad10.message.application.dto.res;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.devsquad10.message.domain.model.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageGetOneResponseDto implements Serializable {
	// TODO: dto 개선

	private UUID id;
	private String message;
	private LocalDateTime createdAt;
	private UUID createdBy;
	private LocalDateTime updatedAt;
	private UUID updatedBy;
	private LocalDateTime deletedAt;
	private UUID deletedBy;

	public static MessageGetOneResponseDto toResponseDto(Message message) {
		return MessageGetOneResponseDto.builder()
			.id(message.getId())
			.message(message.getMessage())
			.createdAt(message.getCreatedAt())
			.createdBy(message.getCreatedBy())
			.updatedAt(message.getUpdatedAt())
			.updatedBy(message.getUpdatedBy())
			.deletedAt(message.getDeletedAt())
			.deletedBy(message.getDeletedBy())
			.build();
	}
}
