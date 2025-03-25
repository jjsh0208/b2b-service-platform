package com.devsquad10.shipping.presentation.controller;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devsquad10.shipping.application.dto.ShippingAgentResponse;
import com.devsquad10.shipping.application.dto.request.ShippingAgentSearchReqDto;
import com.devsquad10.shipping.application.dto.response.PagedShippingAgentResDto;
import com.devsquad10.shipping.application.dto.response.ShippingAgentResDto;
import com.devsquad10.shipping.application.service.ShippingAgentService;
import com.devsquad10.shipping.infrastructure.client.dto.ShippingAgentFeignClientPatchRequest;
import com.devsquad10.shipping.infrastructure.client.dto.ShippingAgentFeignClientPostRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipping-agent")
public class ShippingAgentController {

	private final ShippingAgentService shippingAgentService;

	// 권한 - MASTER, 담당 HUB
	//유저 feign client 호출하면 배송관리자 생성 endpoint 로 연결
	@PostMapping
	public boolean createShippingAgent(
		@RequestBody ShippingAgentFeignClientPostRequest request) {
		return shippingAgentService.createShippingAgent(request);
	}

	// 권한 - MASTER, 담당 HUB, 담당 DLV_AGENT
	@GetMapping("/{shippingManagerId}")
	public ResponseEntity<ShippingAgentResponse<ShippingAgentResDto>> getShippingAgent(
		@PathVariable(name = "shippingManagerId") UUID shippingManagerId) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(ShippingAgentResponse.success(
				HttpStatus.OK.value(),
				shippingAgentService.getShippingAgentById(shippingManagerId))
			);
	}

	// 권한 - MASTER, 담당 HUB, 담당 DLV_AGENT
	@GetMapping("/search")
	public ResponseEntity<ShippingAgentResponse<PagedShippingAgentResDto>> searchShippingAgents(
		@ModelAttribute @Valid ShippingAgentSearchReqDto request
	) {
		PagedShippingAgentResDto response = shippingAgentService.searchShippingAgents(request);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ShippingAgentResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	// 권한 - MASTER, 담당HUB
	// 1.유저 feign client 호출하여 넘겨받은 정보 변경
	@PatchMapping("/info-update")
	public boolean infoUpdateShippingAgent(@RequestBody ShippingAgentFeignClientPatchRequest request) {
		return shippingAgentService.infoUpdateShippingAgent(request);
	}

	// 권한 - MASTER, 담당HUB
	// 2.배송 여부 확인 변경
	@PatchMapping("/transit-update/{shippingManagerId}")
	public ResponseEntity<ShippingAgentResponse<ShippingAgentResDto>> transitUpdateShippingAgent(
		@PathVariable(name = "shippingManagerId") UUID shippingManagerId,
		@RequestParam("isTransit") Boolean isTransit,
		@RequestHeader("X-User-Id") String userId) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(ShippingAgentResponse.success(
				HttpStatus.OK.value(),
				shippingAgentService.transitUpdateShippingAgent(shippingManagerId, isTransit, userId))
			);
	}

	// 권한 - MASTER, 담당HUB
	// 배송담당자 ID로 배송담당자 단일 조회 - User feign client 호출 요청 삭제
	@DeleteMapping("/user/{shippingManagerId}")
	public boolean deleteShippingAgentForUser(
		@PathVariable(name = "shippingManagerId") UUID shippingManagerId) {
		return shippingAgentService.deleteShippingAgentForUser(shippingManagerId);
	}
}
