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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devsquad10.shipping.application.dto.ShippingResponse;
import com.devsquad10.shipping.application.dto.request.ShippingSearchReqDto;
import com.devsquad10.shipping.application.dto.request.ShippingUpdateReqDto;
import com.devsquad10.shipping.application.dto.response.PagedShippingResDto;
import com.devsquad10.shipping.application.dto.response.ShippingResDto;
import com.devsquad10.shipping.application.service.ShippingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipping")
public class ShippingController {

	private final ShippingService shippingService;

	// TODO: 권한 확인 - MASTER, 담당 HUB, DVL_AGENT
	@PatchMapping("/{id}")
	public ResponseEntity<ShippingResponse<ShippingResDto>> statusUpdateShipping(
		@PathVariable(name = "id") UUID id,
		@RequestBody ShippingUpdateReqDto shippingUpdateReqDto) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(ShippingResponse.success(
				HttpStatus.OK.value(),
				shippingService.statusUpdateShipping(id, shippingUpdateReqDto))
			);

	}

	// 업체배송담당자 할당 - ID update
	@PatchMapping("/allocation/{id}")
	public ResponseEntity<ShippingResponse<?>> allocationShipping(@PathVariable(name = "id") UUID id) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(ShippingResponse.success(
				HttpStatus.OK.value(),
				shippingService.allocationShipping(id)));
	}

	// TODO: 권한 확인 - ALL + 담당 HUB, DVL_AGENT
	@GetMapping("/{id}")
	public ResponseEntity<ShippingResponse<ShippingResDto>> getShippingById(
		@PathVariable(name = "id") UUID id) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(ShippingResponse.success(
				HttpStatus.OK.value(),
				shippingService.getShippingById(id))
			);
	}

	// TODO: 권한 확인 - ALL + 담당 HUB, DVL_AGENT
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

	// TODO: 권한 확인 - MASTER, 담당 HUB
	@DeleteMapping("/order/{orderId}")
	public boolean deleteShippingForOrder(
		@PathVariable(name = "orderId") UUID orderId) {
		return shippingService.deleteShippingForOrder(orderId);
	}
}
