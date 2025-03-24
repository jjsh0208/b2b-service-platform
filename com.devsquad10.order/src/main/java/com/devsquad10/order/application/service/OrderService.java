package com.devsquad10.order.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.order.application.dto.OrderReqDto;
import com.devsquad10.order.application.dto.OrderResDto;
import com.devsquad10.order.application.dto.OrderUpdateReqDto;
import com.devsquad10.order.application.dto.PageOrderResponseDto;
import com.devsquad10.order.application.dto.message.ShippingUpdateRequest;
import com.devsquad10.order.application.dto.message.StockDecrementMessage;
import com.devsquad10.order.application.dto.message.StockReversalMessage;
import com.devsquad10.order.application.exception.OrderNotFoundException;
import com.devsquad10.order.application.messaging.OrderMessageService;
import com.devsquad10.order.domain.enums.OrderStatus;
import com.devsquad10.order.domain.model.Order;
import com.devsquad10.order.domain.repository.OrderQuerydslRepository;
import com.devsquad10.order.domain.repository.OrderRepository;
import com.devsquad10.order.infrastructure.client.CompanyClient;
import com.devsquad10.order.infrastructure.client.ShippingClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderQuerydslRepository orderQuerydslRepository;
	private final OrderMessageService orderMessageService;
	private final CompanyClient companyClient;
	private final ShippingClient shippingClient;

	@CachePut(cacheNames = "orderCache", key = "#result.id")
	public OrderResDto createOrder(OrderReqDto orderReqDto) {
		Order order = Order.builder()
			.recipientsId(orderReqDto.getRecipientsId())
			.productId(orderReqDto.getProductId())
			.quantity(orderReqDto.getQuantity())
			.requestDetails(orderReqDto.getRequestDetails())
			.deadLine(orderReqDto.getDeadLine())
			.status(OrderStatus.ORDER_RECEIVED)
			.build();

		// DB에 저장
		orderRepository.save(order);

		orderMessageService.sendStockDecrementMessage(order.toStockDecrementMessage());

		return order.toResponseDto();
	}

	@CachePut(cacheNames = "orderCache", key = "#id")
	@Transactional(readOnly = true)
	public OrderResDto getOrderById(UUID id) {
		return orderRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new OrderNotFoundException("Order Not Found By Id : " + id))
			.toResponseDto();
	}

	@Cacheable(cacheNames = "orderSearchCache", key = "#q + '-' + #category + '-' + #page + '-' + #size")
	@Transactional(readOnly = true)
	public PageOrderResponseDto searchOrders(String q, String category, int page, int size, String sort, String order) {

		Page<Order> orderPages = orderQuerydslRepository.findAll(q, category, page, size, sort, order);

		Page<OrderResDto> orderResDtoPages = orderPages.map(Order::toResponseDto);

		return PageOrderResponseDto.toResponse(orderResDtoPages);

	}

	@CachePut(cacheNames = "orderCache", key = "#id")
	@Caching(evict = {
		@CacheEvict(cacheNames = "orderSearchCache", allEntries = true)
	})
	public OrderResDto updateOrder(UUID id, OrderUpdateReqDto orderUpdateReqDto) {

		Order order = orderRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new OrderNotFoundException("Order Not Found By Id : " + id));

		String newRecipientsAddress = companyClient.findRecipientAddressByCompanyId(order.getRecipientsId());

		//1. 배송지 변경 여부 확인
		if (!order.getRecipientsId().equals(orderUpdateReqDto.getRecipientsId())) {
			newRecipientsAddress =
				companyClient.findRecipientAddressByCompanyId(orderUpdateReqDto.getRecipientsId());
			if (newRecipientsAddress == null) {
				throw new IllegalArgumentException(
					"Invalid recipient address for recipientsId: " + orderUpdateReqDto.getRecipientsId());
			}

			order = order.toBuilder()
				.recipientsId(orderUpdateReqDto.getRecipientsId())
				.build();
		}

		//2. 수량도 변경되면 원래 재고 와 비교하여 감소되면 감소 줄어들면 재고 회복 메시지 전달
		int originalQuantity = order.getQuantity();
		int updatedQuantity = orderUpdateReqDto.getQuantity();

		if (originalQuantity != updatedQuantity) {
			// 수량이 증가하면 재고 감소 요청
			if (updatedQuantity > originalQuantity) {
				int quantityToDecrease = updatedQuantity - originalQuantity;
				StockDecrementMessage stockDecrementMessage = StockDecrementMessage.builder()
					.orderId(order.getId())
					.productId(order.getProductId())
					.quantity(quantityToDecrease) // 감소할 수량
					.build();
				orderMessageService.sendStockDecrementMessage(stockDecrementMessage);// 재고 감소 메시지 전송
			}
			// 수량이 감소하면 재고 회복 요청
			else if (updatedQuantity < originalQuantity) {
				int quantityToRecover = originalQuantity - updatedQuantity;
				StockReversalMessage stockReversalMessage = new StockReversalMessage(order.getProductId(),
					quantityToRecover);
				orderMessageService.sendStockReversalMessage(stockReversalMessage);
			}

			// 주문 수량 업데이트
			order = order.toBuilder()
				.quantity(updatedQuantity)
				.build();
		}

		// 4.  기타 정보 업데이트
		order = order.toBuilder()
			.requestDetails(orderUpdateReqDto.getRequestDetails())  // 요청 사항 업데이트
			.deadLine(orderUpdateReqDto.getDeadLine())              // 납품 기한일자 업데이트
			.build();

		ShippingUpdateRequest shippingUpdateRequest = ShippingUpdateRequest.builder()
			.orderId(order.getId())
			.recipientsId(order.getRecipientsId())
			.address(newRecipientsAddress)
			.requestDetails(orderUpdateReqDto.getRequestDetails())
			.deadLine(orderUpdateReqDto.getDeadLine())
			.build();

		orderMessageService.sendShippingUpdateMessage(shippingUpdateRequest);

		// 5. 업데이트된 주문 정보 저장
		orderRepository.save(order);

		// 6. 업데이트된 주문 정보 반환
		return order.toResponseDto();
	}

	@Caching(evict = {
		@CacheEvict(cacheNames = "orderCache", key = "#id"),
		@CacheEvict(cacheNames = "orderSearchCache", key = "#id")
	})
	public void deleteOrder(UUID id) {
		Order order = orderRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new OrderNotFoundException("Order Not Found By Id : " + id));

		StockReversalMessage stockReversalMessage = new StockReversalMessage(order.getProductId(),
			order.getQuantity());

		orderMessageService.sendStockReversalMessage(stockReversalMessage);
		shippingClient.deleteShippingForOrder(id);

		orderRepository.save(order.toBuilder()
			.deletedAt(LocalDateTime.now())
			.deletedBy("사용자")
			.build());
	}
}
