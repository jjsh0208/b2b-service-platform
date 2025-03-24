package com.devsquad10.message.application.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackMessageRequestDto {
	String channel;
	String receiverId;
	String message;
}
