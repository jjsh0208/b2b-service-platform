package com.devsquad10.message.infrastructure.client.dto;

import java.time.LocalDateTime;

import com.devsquad10.message.domain.model.Message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingClientDataResponseDto {
	private String messageId;
	private String recipientId;
	private String message;
	private LocalDateTime createdAt;

	public static ShippingClientDataResponseDto fromEntity(Message message) {
		return ShippingClientDataResponseDto.builder()
			.messageId(String.valueOf(message.getId()))
			.recipientId(message.getRecipientId())
			.message(message.getMessage())
			.createdAt(message.getCreatedAt())
			.build();
	}
}
