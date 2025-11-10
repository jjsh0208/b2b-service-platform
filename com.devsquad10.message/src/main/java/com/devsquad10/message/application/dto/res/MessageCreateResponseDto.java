package com.devsquad10.message.application.dto.res;

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
public class MessageCreateResponseDto {

	// TODO: dto 개선

	private UUID id;
	private LocalDateTime createdAt;
	private UUID createdBy;
	private LocalDateTime updatedAt;
	private UUID updatedBy;

	public static MessageCreateResponseDto toResponseDto(Message message) {
		return MessageCreateResponseDto.builder()
			.id(message.getId())
			.createdAt(message.getCreatedAt())
			.createdBy(message.getCreatedBy())
			.updatedAt(message.getUpdatedAt())
			.updatedBy(message.getUpdatedBy())
			.build();
	}
}
