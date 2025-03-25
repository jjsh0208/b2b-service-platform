package com.devsquad10.shipping.infrastructure.client.dto;

import java.time.LocalDateTime;
import java.util.UUID;

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
}
