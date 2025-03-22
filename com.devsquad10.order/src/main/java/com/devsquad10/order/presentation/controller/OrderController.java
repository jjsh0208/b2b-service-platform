package com.devsquad10.order.presentation.controller;

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

import com.devsquad10.order.application.dto.OrderReqDto;
import com.devsquad10.order.application.dto.OrderResDto;
import com.devsquad10.order.application.dto.OrderUpdateReqDto;
import com.devsquad10.order.application.dto.PageOrderResponseDto;
import com.devsquad10.order.application.dto.response.OrderResponse;
import com.devsquad10.order.application.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<OrderResponse<OrderResDto>> createOrder(@RequestBody OrderReqDto orderReqDto) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(OrderResponse.success(HttpStatus.OK.value(), orderService.createOrder(orderReqDto)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse<OrderResDto>> getOrderById(@PathVariable("id") UUID id) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(OrderResponse.success(HttpStatus.OK.value(), orderService.getOrderById(id)));
	}

	@GetMapping("/search")
	public ResponseEntity<OrderResponse<PageOrderResponseDto>> searchOrders(
		@RequestParam(required = false) String q,
		@RequestParam(required = false) String category,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String order) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(
				OrderResponse.success(HttpStatus.OK.value(),
					orderService.searchOrders(q, category, page, size, sort, order)));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<OrderResponse<OrderResDto>> updateOrder(@PathVariable("id") UUID id,
		@RequestBody OrderUpdateReqDto orderUpdateReqDto) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(OrderResponse.success(HttpStatus.OK.value(), orderService.updateOrder(id, orderUpdateReqDto)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<OrderResponse<String>> deleteOrder(@PathVariable("id") UUID id) {
		orderService.deleteOrder(id);
		return ResponseEntity.status(HttpStatus.OK)
			.body(OrderResponse.success(HttpStatus.OK.value(), "Order Deleted successfully"));
	}

}
