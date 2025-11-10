package com.devsquad10.product.application.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductResponse<T> {
	private final int status;   // HTTP 상태 코드
	private final boolean success; // 성공 여부
	private final T body;       // 실제 응답 데이터

	// 성공 응답 생성
	public static <T> ProductResponse<T> success(int status, T body) {
		return new ProductResponse<>(status, true, body);
	}

	// 실패 응답 생성
	public static <T> ProductResponse<T> failure(int status, T body) {
		return new ProductResponse<>(status, false, body);
	}
}
