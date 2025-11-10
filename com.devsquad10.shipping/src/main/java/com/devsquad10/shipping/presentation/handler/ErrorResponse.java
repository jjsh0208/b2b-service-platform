package com.devsquad10.shipping.presentation.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
	private int status; // HTTP 상태코드
	private boolean success; // 성공 여부
	private String message; // 에러 메시지
}