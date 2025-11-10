package com.devsquad10.message.application.dto.res;

import java.time.LocalDateTime;
import java.util.UUID;

import com.devsquad10.message.domain.model.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MessageResponseDto {
	private UUID id;
	private String message;
	private String recipientId;
	private LocalDateTime createdAt;

	public static MessageResponseDto fromEntity(Message message) {
		return MessageResponseDto.builder()
			.id(message.getId())
			.message(message.getMessage())
			.recipientId(message.getRecipientId())
			.createdAt(message.getCreatedAt())
			.build();
	}
}
