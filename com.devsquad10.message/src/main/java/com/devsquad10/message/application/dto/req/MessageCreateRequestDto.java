package com.devsquad10.message.application.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequestDto {
	@NotBlank
	private String name;

	@NotBlank
	private String recipientId;
}
