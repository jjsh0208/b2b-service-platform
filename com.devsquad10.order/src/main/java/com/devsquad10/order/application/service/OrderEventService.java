package com.devsquad10.order.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.devsquad10.order.application.dto.message.ShippingCreateRequest;
import com.devsquad10.order.application.dto.message.ShippingResponseMessage;
import com.devsquad10.order.application.dto.message.StockDecrementMessage;
import com.devsquad10.order.application.dto.message.StockReversalMessage;
import com.devsquad10.order.application.exception.OrderNotFoundException;
import com.devsquad10.order.application.messaging.OrderMessageService;
import com.devsquad10.order.domain.enums.OrderStatus;
import com.devsquad10.order.domain.model.Order;
import com.devsquad10.order.domain.repository.OrderRepository;
import com.devsquad10.order.infrastructure.client.CompanyClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderEventService {

	private final OrderRepository orderRepository;
	private final CompanyClient companyClient;
	private final OrderMessageService orderMessageService;
	private final RedisTemplate<String, String> redisTemplate;

	private static final String RETRY_COUNT_KEY_PREFIX = "shipping_retry_count:";

	/**
	 * 재고 차감 메시지가 수신되면 해당 주문에 대해 배송 요청을 처리한다.
	 * 주문이 정상저그올 생성되고 재고가 차감되었을 경우 배송 생성 메시지를 전달한다.
	 *
	 * @param stockDecrementMessage 재고 차감 메시지
	 */
	public void handlerShippingRequest(StockDecrementMessage stockDecrementMessage) {
		Order targetOrder = findOrderById(stockDecrementMessage.getOrderId());
		Optional<String> recipientsAddress = findRecipientAddress(targetOrder.getRecipientsId());

		processShippingRequest(targetOrder, stockDecrementMessage, recipientsAddress);
	}

	/**
	 * 재고 차감 메시지가 수신되면 주문 상태를 업데이트한다.
	 * 만약 재고가 부족하거나 차감할 수 없는 경우 주문 상태를 전달받은 "OUT_OF_STOCK" 으로 변경한다.
	 *
	 * @param stockDecrementMessage 재고 차감 메시지
	 */
	public void updateOrderStatus(StockDecrementMessage stockDecrementMessage) {
		Order targetOrder = findOrderById(stockDecrementMessage.getOrderId());
		updateOrderStatus(targetOrder, OrderStatus.fromString(stockDecrementMessage.getStatus()));
	}

	/**
	 * 배송 생성이 성공하면  "WAITING_FOR_SHIPMENT" (배송 대기) 상태로 변경한다.
	 *
	 * @param shippingResponseMessage 배송 생성 응답 메시지
	 */
	public void updateOrderStatusToWaitingForShipment(ShippingResponseMessage shippingResponseMessage) {
		Order targetOrder = findOrderById(shippingResponseMessage.getOrderId());
		updateOrderStatus(targetOrder, OrderStatus.WAITING_FOR_SHIPMENT);
	}

	/**
	 * 배송 생성 실패 시 재시도 요청을 한다.
	 *
	 * @param shippingResponseMessage 배송 생성 응답 메시지
	 */
	public void retryCreateShipping(ShippingResponseMessage shippingResponseMessage) {

		// Redis에서 재시도 카운트를 가져옴
		String orderId = shippingResponseMessage.getOrderId().toString();
		String retryCountKey = RETRY_COUNT_KEY_PREFIX + orderId;

		String retryCountStr = redisTemplate.opsForValue().get(retryCountKey);
		int retryCount = (retryCountStr != null) ? Integer.parseInt(retryCountStr) : 0;

		// 3회 이상 재시도한 경우 상태를 'ORDER_RECEIVED'로 변경하고 종료
		if (retryCount >= 3) {
			updateOrderStatus(findOrderById(shippingResponseMessage.getOrderId()), OrderStatus.ORDER_FAILED);
			return;
		}

		Order targetOrder = findOrderById(shippingResponseMessage.getOrderId());
		Optional<String> recipientsAddress = findRecipientAddress(targetOrder.getRecipientsId());

		StockDecrementMessage stockDecrementMessage = StockDecrementMessage.builder()
			.supplierId(targetOrder.getSupplierId())
			.build();

		processShippingRequest(targetOrder, stockDecrementMessage, recipientsAddress);

		// Redis에 재시도 카운트를 저장
		redisTemplate.opsForValue().set(retryCountKey, String.valueOf(retryCount + 1));
	}

	/*** 공통 로직 ***/

	/**
	 * 주문 ID를 이용해 주문을 조회한다.
	 *
	 * @param orderId 주문 ID
	 * @return 조회된 주문 객체
	 * @throws OrderNotFoundException 주문을 찾을 수 없을 경우 예외 발생
	 */
	private Order findOrderById(UUID orderId) {
		return orderRepository.findByIdAndDeletedAtIsNull(orderId)
			.orElseThrow(() -> new OrderNotFoundException("Order Not Found By Id : " + orderId));
	}

	/**
	 * 수령 업체 ID로 수령 업체의 주소를 조회한다.
	 *
	 * @param recipientsId 수령인 ID
	 * @return 수령인의 주소 (Optional)
	 */
	private Optional<String> findRecipientAddress(UUID recipientsId) {
		return Optional.ofNullable(companyClient.findRecipientAddressByCompanyId(recipientsId));
	}

	/**
	 * 배송 요청을 처리한다.
	 * 수령인 주소가 있으면 배송을 생성하고, 없으면 재고 반품 메시지를 전달한다.
	 *
	 * @param targetOrder 대상 주문
	 * @param stockDecrementMessage 재고 차감 메시지
	 * @param recipientsAddress 수령인 주소 (Optional)
	 */
	private void processShippingRequest(Order targetOrder, StockDecrementMessage stockDecrementMessage,
		Optional<String> recipientsAddress) {
		recipientsAddress.ifPresentOrElse(
			address -> {
				updateOrderStatus(targetOrder, stockDecrementMessage, OrderStatus.PREPARING_SHIPMENT);
				ShippingCreateRequest shippingCreateRequest = createShippingRequest(targetOrder, stockDecrementMessage,
					address);
				orderMessageService.sendShippingCreateMessage(shippingCreateRequest);
			},
			() -> {
				updateOrderStatus(targetOrder, stockDecrementMessage, OrderStatus.INVALID_RECIPIENT);
				orderMessageService.sendStockReversalMessage(
					new StockReversalMessage(targetOrder.getProductId(), targetOrder.getQuantity()));
			}
		);
	}

	/**
	 * 주문 상태를 업데이트한다.
	 *
	 * @param targetOrder 대상 주문
	 * @param newStatus 새로운 주문 상태
	 */
	private void updateOrderStatus(Order targetOrder, OrderStatus newStatus) {
		orderRepository.save(targetOrder.toBuilder().status(newStatus).build());
	}

	/**
	 * 주문 상태 및 배송 관련 정보를 업데이트한다.
	 *
	 * @param targetOrder 대상 주문
	 * @param stockDecrementMessage 재고 차감 메시지
	 * @param newStatus 새로운 주문 상태
	 */
	private void updateOrderStatus(Order targetOrder, StockDecrementMessage stockDecrementMessage,
		OrderStatus newStatus) {
		orderRepository.save(targetOrder.toBuilder()
			.supplierId(stockDecrementMessage.getSupplierId())
			.productName(stockDecrementMessage.getProductName())
			.totalAmount(stockDecrementMessage.getPrice() * targetOrder.getQuantity())
			.status(newStatus)
			.build());
	}

	/**
	 * 배송 요청 메시지를 생성한다.
	 *
	 * @param order 주문 객체
	 * @param message 재고 차감 메시지
	 * @param address 배송지 주소
	 * @return 생성된 배송 요청 메시지
	 */
	private ShippingCreateRequest createShippingRequest(Order order, StockDecrementMessage message, String address) {
		return new ShippingCreateRequest(
			order.getId(),
			message.getSupplierId(),
			order.getRecipientsId(),
			address,
			order.getRequestDetails(),
			order.getDeadLine()
		);
	}
}
