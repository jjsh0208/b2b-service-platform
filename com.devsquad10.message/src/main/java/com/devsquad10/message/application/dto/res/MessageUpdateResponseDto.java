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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageUpdateResponseDto implements Serializable {

	// TODO: dto 개선

	private UUID id;
	private LocalDateTime createdAt;
	private UUID createdBy;
	private LocalDateTime updatedAt;
	private UUID updatedBy;
	private LocalDateTime deletedAt;
	private UUID deletedBy;

	public static MessageUpdateResponseDto toResponseDto(Message message) {
		return MessageUpdateResponseDto.builder()
			.id(message.getId())
			.createdAt(message.getCreatedAt())
			.createdBy(message.getCreatedBy())
			.updatedAt(message.getUpdatedAt())
			.updatedBy(message.getUpdatedBy())
			.deletedAt(message.getDeletedAt())
			.deletedBy(message.getDeletedBy())
			.build();
	}
}
