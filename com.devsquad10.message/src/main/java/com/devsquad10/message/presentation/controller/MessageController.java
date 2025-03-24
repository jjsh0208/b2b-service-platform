package com.devsquad10.message.presentation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devsquad10.message.application.dto.req.MessageCreateRequestDto;
import com.devsquad10.message.application.dto.req.MessageSearchRequestDto;
import com.devsquad10.message.application.dto.req.MessageUpdateRequestDto;
import com.devsquad10.message.application.dto.req.SlackMessageRequestDto;
import com.devsquad10.message.application.dto.res.ApiResponse;
import com.devsquad10.message.application.dto.res.MessageCreateResponseDto;
import com.devsquad10.message.application.dto.res.MessageGetOneResponseDto;
import com.devsquad10.message.application.dto.res.MessageUpdateResponseDto;
import com.devsquad10.message.application.dto.res.PagedMessageResponseDto;
import com.devsquad10.message.application.service.MessageService;
import com.devsquad10.message.application.service.SlackService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

	private final MessageService messageService;
	private final SlackService slackService;

	@PostMapping
	public ResponseEntity<ApiResponse<MessageCreateResponseDto>> createMessage(
		@Valid @RequestBody MessageCreateRequestDto request
	) {
		MessageCreateResponseDto response = messageService.createMessage(request);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<MessageGetOneResponseDto>> getMessage(
		@PathVariable UUID id
	) {
		MessageGetOneResponseDto response = messageService.getOneMessage(id);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ApiResponse<MessageUpdateResponseDto>> updateMessage(
		@PathVariable UUID id,
		@Valid @RequestBody MessageUpdateRequestDto request
	) {
		MessageUpdateResponseDto response = messageService.updateMessage(id, request);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<String>> deleteMessage(
		@PathVariable UUID id
	) {
		messageService.deleteMessage(id);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				"Message successfully deleted"
			));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PagedMessageResponseDto>> getAllMessages(
		@ModelAttribute @Valid MessageSearchRequestDto request
	) {
		PagedMessageResponseDto response = messageService.getMessage(request);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	@PostMapping("/slack")
	public void sendMessage(@RequestBody SlackMessageRequestDto slackMessageReqDto) {
		slackService.sendMessage(slackMessageReqDto);
	}
}


