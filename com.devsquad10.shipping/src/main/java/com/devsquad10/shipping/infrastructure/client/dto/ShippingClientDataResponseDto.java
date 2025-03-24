package com.devsquad10.shipping.infrastructure.client.dto;

import java.time.LocalDateTime;

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
}
