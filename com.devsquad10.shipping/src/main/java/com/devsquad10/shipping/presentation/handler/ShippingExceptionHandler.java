package com.devsquad10.shipping.presentation.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.devsquad10.shipping.application.dto.ShippingResponse;
import com.devsquad10.shipping.application.exception.shipping.InvalidShippingStatusUpdateException;
import com.devsquad10.shipping.application.exception.shipping.ShippingCreateException;
import com.devsquad10.shipping.application.exception.shipping.ShippingNotFoundException;

@RestControllerAdvice
public class ShippingExceptionHandler {

	// 배송,배송 경로 기록 - 커스텀 예외 처리
	// 배송Id 존재X
	@ExceptionHandler(ShippingNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ShippingResponse<String>> handlerShippingNotFoundException(ShippingNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ShippingResponse.failure(HttpStatus.NOT_FOUND.value(), e.getMessage()));
	}
	// 배송 생성 시, 실패
	@ExceptionHandler(ShippingCreateException.class)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<ShippingResponse<String>> handlerShippingCreateException(ShippingCreateException e) {
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
			.body(ShippingResponse.failure(HttpStatus.NO_CONTENT.value(), e.getMessage()));
	}

	// 배송 상태 이전 상태로 업데이트 불가
	@ExceptionHandler(InvalidShippingStatusUpdateException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ShippingResponse<String>> handlerInvalidShippingStatusUpdateException(InvalidShippingStatusUpdateException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ShippingResponse.failure(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
	}
}