package com.devsquad10.order.presentation.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.devsquad10.order.application.dto.response.OrderResponse;
import com.devsquad10.order.application.exception.OrderNotFoundException;

@RestControllerAdvice
public class OrderExceptionHandler {

	@ExceptionHandler(OrderNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<OrderResponse<String>> orderNotFoundException(OrderNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(OrderResponse.failure(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST) // 400 BAD_REQUEST 응답 상태 코드
	public ResponseEntity<OrderResponse<String>> illegalArgumentException(IllegalArgumentException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(OrderResponse.failure(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<OrderResponse<String>> exception(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(OrderResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
	}
}
