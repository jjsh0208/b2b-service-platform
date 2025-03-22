package com.devsquad10.shipping.presentation.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.devsquad10.shipping.application.dto.ShippingAgentResponse;
import com.devsquad10.shipping.application.exception.shippingAgent.HubIdNotFoundException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentAlreadyAllocatedException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentNotAllocatedException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentNotFoundException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentTypeNotFoundException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAssignmentCountException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingStatusIsNotAllocatedException;
import com.devsquad10.shipping.application.exception.shippingAgent.SlackMessageSendToDesHubManagerIdException;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class ShippingAgentExceptionHandler {

	// 배송 담당자 - 커스텀 예외 처리
	// 담당자 타입 존재X
	@ExceptionHandler(ShippingAgentNotFoundException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<ShippingAgentResponse<String>> handlerShippingAgentNotFoundException(ShippingAgentNotFoundException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ShippingAgentResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
	}

	// 담당자 타입 존재X
	@ExceptionHandler(ShippingAgentTypeNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ShippingAgentResponse<String>> handlerShippingAgentTypeNotFoundException(ShippingAgentTypeNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ShippingAgentResponse.failure(HttpStatus.NOT_FOUND.value(), e.getMessage()));
	}

	// 허브Id 존재X
	@ExceptionHandler(HubIdNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ShippingAgentResponse<String>> handlerHubIdNotFoundException(HubIdNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ShippingAgentResponse.failure(HttpStatus.NOT_FOUND.value(), e.getMessage()));
	}

	// 배정할 배송 담당자가 존재X
	@ExceptionHandler(ShippingAgentNotAllocatedException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ShippingAgentResponse<String>> handlerShippingAgentNotAllocatedException(ShippingAgentNotAllocatedException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ShippingAgentResponse.failure(HttpStatus.NOT_FOUND.value(), e.getMessage()));
	}
	// 배송 상태가 배송 담당자 배정 불가
	@ExceptionHandler(ShippingStatusIsNotAllocatedException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ShippingAgentResponse<String>> handlerShippingStatusIsNotAllocatedException(
		ShippingStatusIsNotAllocatedException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ShippingAgentResponse.failure(HttpStatus.NOT_FOUND.value(), e.getMessage()));
	}

	// 해당 허브ID 에 배송가능한 업체 담당자 없음
	@ExceptionHandler(SlackMessageSendToDesHubManagerIdException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ShippingAgentResponse<String>> handlerSlackMessageSendToDesHubManagerIdException(
		SlackMessageSendToDesHubManagerIdException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ShippingAgentResponse.failure(HttpStatus.NOT_FOUND.value(), e.getMessage()));
	}

	// 업체 배송담당자ID를 이미 배정받은 상태
	@ExceptionHandler(ShippingAgentAlreadyAllocatedException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ResponseEntity<ShippingAgentResponse<String>> handlerShippingAgentAlreadyAllocatedException(
		ShippingAgentAlreadyAllocatedException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(ShippingAgentResponse.failure(HttpStatus.CONFLICT.value(), e.getMessage()));
	}

	// 배정 횟수 증가 시, 변경에 문제 발생
	@ExceptionHandler(ShippingAssignmentCountException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ResponseEntity<ShippingAgentResponse<String>> handlerShippingAssignmentCountException(
		ShippingAssignmentCountException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(ShippingAgentResponse.failure(HttpStatus.CONFLICT.value(), e.getMessage()));
	}

	// 허브 응답 받은 json null
	@ExceptionHandler(EntityNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ShippingAgentResponse<String>> handlerEntityNotFoundException(EntityNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ShippingAgentResponse.failure(HttpStatus.NOT_FOUND.value(), e.getMessage()));
	}
}