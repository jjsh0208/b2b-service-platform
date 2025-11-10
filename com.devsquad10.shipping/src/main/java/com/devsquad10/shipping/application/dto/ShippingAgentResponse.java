package com.devsquad10.shipping.application.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ShippingAgentResponse<T> {
	private final int status; // HTTP 상태 코드
	private final boolean success; // 성공여부
	private final T body; // 실제 응답 데이터

	// 성공 응답 생성
	public static <T> ShippingAgentResponse<T> success(int status, T body) {
		return new ShippingAgentResponse<>(status, true, body);
	}

	// 실패 응답 생성
	public static <T> ShippingAgentResponse<T> failure(int status, T body) {
		return new ShippingAgentResponse<>(status, false, body);
	}
}