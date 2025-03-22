package com.devsquad10.shipping.presentation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devsquad10.shipping.application.dto.ShippingAgentResponse;
import com.devsquad10.shipping.application.dto.response.ShippingAgentResDto;
import com.devsquad10.shipping.application.service.ShippingAgentService;
import com.devsquad10.shipping.infrastructure.client.ShippingAgentFeignClientPatchRequest;
import com.devsquad10.shipping.infrastructure.client.ShippingAgentFeignClientPostRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipping-agent")
public class ShippingAgentController {

	private final ShippingAgentService shippingAgentService;

	//TODO: 유저 feign client 호출하면 배송관리자 생성 endpoint 로 연결
	// 권한 확인 - MASTER, 담당 HUB
	@PostMapping
	public void createShippingAgent(
		@RequestBody ShippingAgentFeignClientPostRequest request) {
		shippingAgentService.createShippingAgent(request);
	}

	// TODO: 권한 확인 - MASTER, 담당 HUB, 담당 DLV_AGENT
	@GetMapping("/{id}")
	public ResponseEntity<ShippingAgentResponse<ShippingAgentResDto>> getShippingAgent(
		@PathVariable(name = "id") UUID id) {

		return ResponseEntity.ok(ShippingAgentResponse.success(
				HttpStatus.OK.value(),
				shippingAgentService.getShippingAgentById(id))
			);
	}

	// TODO: 권한 확인 - MASTER, 담당 HUB, 담당 DLV_AGENT
	// public ResponseEntity<Page<ShippingAgentResDto>> searchShippingAgents(
	// 	@RequestParam(name = "query", required = false) String query,
	// 	@RequestParam(name = "category", required = false) String category,
	// 	@PageableDefault(page = 0, size = 10, sort = "createdBy", direction = Sort.Direction.DESC) Pageable pageable) {
	// 	// @RequestParam(name = "page", defaultValue = "0") int page,
	// 	// @RequestParam(name = "size", defaultValue = "10") int size,
	// 	// @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
	// 	// @RequestParam(name = "orderBy", defaultValue = "desc") String orderBy) {
	//
	// 	return ResponseEntity.ok(
	// 		shippingAgentService.searchShippingAgents(
	// 			query,
	// 			category,
	// 			pageable
	// 		));
	//
	// 	// return ResponseEntity.ok(
	// 	// 	shippingAgentService.searchShippingAgents(
	// 	// 		query,
	// 	// 		category,
	// 	// 		page,
	// 	// 		size,
	// 	// 		sortBy,
	// 	// 		orderBy
	// 	// 	));
	// }

	//TODO: 권한 확인 - MASTER, 담당HUB
	// 1.유저 feign client 호출하여 넘겨받은 정보 변경
	@PatchMapping("/info-update")
	public void infoUpdateShippingAgent(@RequestBody ShippingAgentFeignClientPatchRequest request) {
		shippingAgentService.infoUpdateShippingAgent(request);
	}

	// TODO: 권한 확인 - MASTER, 담당HUB
	// 2.배송 여부 확인 변경
	@PatchMapping("/transit-update/{id}")
	public ResponseEntity<ShippingAgentResponse<ShippingAgentResDto>> transitUpdateShippingAgent(
		@PathVariable(name = "id") UUID id,
		@RequestParam("isTransit") Boolean isTransit) {

		return ResponseEntity.ok(ShippingAgentResponse.success(
			HttpStatus.OK.value(),
			shippingAgentService.transitUpdateShippingAgent(id, isTransit))
		);
	}

	// TODO: 권한 확인 - MASTER, 담당HUB
	@DeleteMapping("{id}")
	public void deleteShippingAgent(@PathVariable(name = "id") UUID id) {
		shippingAgentService.deleteShippingAgent(id);
	}

	// TODO: 배송담당자ID로 배송담당자 단일 조회 - User 삭제 시, feign client 호출 요청용
}
