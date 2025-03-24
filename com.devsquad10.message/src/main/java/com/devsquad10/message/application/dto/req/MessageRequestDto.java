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
public class MessageRequestDto {
	@NotBlank(message = "채널 정보는 필수입니다.")
	private String channel;

	@NotBlank(message = "수신자 ID는 필수입니다.")
	private String receiverId;

	@NotBlank(message = "메시지 내용은 필수입니다.")
	private String message;
}
