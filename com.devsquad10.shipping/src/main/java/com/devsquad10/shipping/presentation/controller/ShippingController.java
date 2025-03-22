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

import com.devsquad10.shipping.application.dto.request.ShippingPostReqDto;
import com.devsquad10.shipping.application.dto.response.ShippingResDto;
import com.devsquad10.shipping.application.dto.ShippingResponse;
import com.devsquad10.shipping.application.dto.request.ShippingUpdateReqDto;
import com.devsquad10.shipping.application.service.ShippingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shipping")
public class ShippingController {

	private final ShippingService shippingService;

	// // TODO: 권한 확인 - MASTER
	// // TODO: 주문 생성 시, 메시지 전달 endpoint 로 변경 예정
	// @PostMapping
	// public ResponseEntity<ShippingResponse<ShippingResDto>> shipping(
	// 	@RequestBody ShippingPostReqDto shippingReqDto) {
	//
	// 	return ResponseEntity.status(HttpStatus.OK)
	// 		.body(ShippingResponse.success(
	// 			HttpStatus.OK.value(),
	// 			shippingService.createShipping(shippingReqDto))
	// 		);
	// }

	// TODO: 권한 확인 - MASTER, 담당 HUB, DVL_AGENT
	@PatchMapping("/{id}")
	public ResponseEntity<ShippingResponse<?>> updateShipping(
		@PathVariable(name = "id") UUID id,
		@RequestBody ShippingUpdateReqDto shippingUpdateReqDto) {

		try {
			if(shippingUpdateReqDto.getStatus() != null) {
				return ResponseEntity.status(HttpStatus.OK)
					.body(ShippingResponse.success(
						HttpStatus.OK.value(),
						shippingService.statusUpdateShipping(id, shippingUpdateReqDto))
					);
			} else if(shippingUpdateReqDto.getAddress() != null
				|| shippingUpdateReqDto.getRequestDetails() != null) {
				return ResponseEntity.status(HttpStatus.OK)
					.body(ShippingResponse.success(
						HttpStatus.OK.value(),
						shippingService.infoUpdateShipping(id, shippingUpdateReqDto))
					);
			} else {
				return ResponseEntity.status(HttpStatus.OK)
					.body(ShippingResponse.success(
						HttpStatus.OK.value(),
						shippingService.updateShipping(id, shippingUpdateReqDto))
					);
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ShippingResponse.failure(
					HttpStatus.BAD_REQUEST.value(),
					"배송 수정 불가능: " + e.getMessage())
				);
		}
	}

	// 배송담당자 할당
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

		return ResponseEntity.ok(ShippingResponse.success(
			HttpStatus.OK.value(),
			shippingService.getShippingById(id))
		);
	}

	// TODO: 권한 확인 - ALL + 담당 HUB, DVL_AGENT
	@GetMapping("/search")
	public ResponseEntity<ShippingResponse<?>> searchShipping(
		@RequestParam(name = "query", required = false) String query,
		@RequestParam(name = "category", required = false) String category,
		@RequestParam(name = "page",defaultValue= "0") int page,
		@RequestParam(name = "size",defaultValue = "10") int size,
		@RequestParam(name = "sortBy", defaultValue = "createdAt") String sort,
		@RequestParam(name = "order", defaultValue = "desc") String order) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(ShippingResponse.success(
				HttpStatus.OK.value(),
				shippingService.searchShipping(query, category, page, size, sort, order))
			);
	}

	// TODO: 권한 확인 - MASTER, 담당 HUB
	@DeleteMapping("/{id}")
	public ResponseEntity<ShippingResponse<String>> deleteShipping(
		@PathVariable(name = "id") UUID id) {

		shippingService.deleteShipping(id);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ShippingResponse.success(
				HttpStatus.OK.value(),
				"배송이 삭제되었습니다.")
			);
	}
}
