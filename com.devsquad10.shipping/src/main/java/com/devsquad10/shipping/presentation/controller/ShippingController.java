package com.devsquad10.shipping.presentation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devsquad10.shipping.application.dto.ShippingResponse;
import com.devsquad10.shipping.application.dto.request.ShippingSearchReqDto;
import com.devsquad10.shipping.application.dto.request.ShippingUpdateReqDto;
import com.devsquad10.shipping.application.dto.response.PagedShippingResDto;
import com.devsquad10.shipping.application.dto.response.ShippingResDto;
import com.devsquad10.shipping.application.service.ShippingService;
import com.devsquad10.shipping.infrastructure.client.dto.ShippingClientDataRequestDto;
import com.devsquad10.shipping.infrastructure.client.dto.ShippingClientDataResponseDto;
import com.devsquad10.shipping.presentation.swagger.ShippingSwaggerDocs;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipping")
public class ShippingController {

	private final ShippingService shippingService;

	// 권한 - MASTER, 담당 HUB, DVL_AGENT
	// 배송 상태(HUB_WAIT -> HUB_TRNS -> HUB_ARV -> COM_TRNS -> DLV_COMP)
	@ShippingSwaggerDocs.StatusUpdateShipping
	@PatchMapping("/{id}")
	public ResponseEntity<ShippingResponse<ShippingResDto>> statusUpdateShipping(
		@PathVariable(name = "id") UUID id,
		@RequestBody ShippingUpdateReqDto shippingUpdateReqDto,
		@RequestHeader("X-User-Id") UUID userId
		) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(ShippingResponse.success(
				HttpStatus.OK.value(),
				shippingService.statusUpdateShipping(id, shippingUpdateReqDto, userId))
			);
	}

	// TODO: 테스트 후, 삭제(함께 슬랙 메시지 발송)
	// // 슬랙 발송 API 테스트
	// @GetMapping("/slack/{orderId}")
	// public ResponseEntity<ShippingResponse<ShippingClientDataResponseDto>> sendSlackMessage(
	// 	@PathVariable(name = "orderId") UUID orderId
	// ) {
	// 	return ResponseEntity.status(HttpStatus.OK)
	// 		.body(ShippingResponse.success(
	// 			HttpStatus.OK.value(),
	// 			shippingService.sendSlackMessage(orderId)));
	// }

	// 권한 - ALL + 담당 HUB, DVL_AGENT
	@ShippingSwaggerDocs.GetShippingById
	@GetMapping("/{id}")
	public ResponseEntity<ShippingResponse<ShippingResDto>> getShippingById(
		@PathVariable(name = "id") UUID id) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(ShippingResponse.success(
				HttpStatus.OK.value(),
				shippingService.getShippingById(id))
			);
	}

	// 권한 - ALL + 담당 HUB, DVL_AGENT
	@ShippingSwaggerDocs.SearchShipping
	@GetMapping("/search")
	public ResponseEntity<ShippingResponse<PagedShippingResDto>> searchShipping(
		@ModelAttribute @Valid ShippingSearchReqDto request
	) {
		PagedShippingResDto response = shippingService.searchShipping(request);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ShippingResponse.success(
				HttpStatus.OK.value(),
				response)
			);
	}

	// 권한 - MASTER, 담당 HUB
	@ShippingSwaggerDocs.DeleteShippingForOrder
	@DeleteMapping("/order/{orderId}")
	public boolean deleteShippingForOrder(
		@PathVariable(name = "orderId") UUID orderId) {
		return shippingService.deleteShippingForOrder(orderId);
	}

	// AI API 배송 데이터 검증 - orderId로 검증
	@ShippingSwaggerDocs.IsShippingDataExists
	@GetMapping("/exists/{uuid}")
	public Boolean isShippingDataExists(@PathVariable(name = "uuid") UUID uuid) {
		return shippingService.isShippingDataExists(uuid);
	}

	// AI 슬랙 알림 전송용 배송 데이터 요청
	@ShippingSwaggerDocs.GetShippingClientData
	@GetMapping("/delivery-notification-data/{orderId}")
	public ShippingClientDataRequestDto getShippingClientData(@PathVariable(name = "orderId") UUID orderId) {
		return shippingService.getShippingClientData(orderId);
	}
}
