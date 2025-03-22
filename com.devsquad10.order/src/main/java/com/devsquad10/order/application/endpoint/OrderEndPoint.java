package com.devsquad10.order.application.endpoint;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.devsquad10.order.application.dto.message.ShippingResponseMessage;
import com.devsquad10.order.application.dto.message.StockDecrementMessage;
import com.devsquad10.order.application.service.OrderEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class OrderEndPoint {

	private final OrderEventService orderEventService;

	@RabbitListener(queues = "${stockMessage.queue.stock.response}")
	public void handlerStockDecrementResponse(StockDecrementMessage stockDecrementMessage) {
		if (stockDecrementMessage.getStatus().equals("SUCCESS")) {
			log.info("재고 차감 성공: 주문 ID = {}, 상품 ID = {}",
				stockDecrementMessage.getOrderId(), stockDecrementMessage.getProductId());
			orderEventService.handlerShippingRequest(stockDecrementMessage);
		} else if (stockDecrementMessage.getStatus().equals("OUT_OF_STOCK")) {
			log.warn("재고 부족: 주문 ID = {}, 상품 ID = {}",
				stockDecrementMessage.getOrderId(), stockDecrementMessage.getProductId());
			orderEventService.updateOrderStatus(stockDecrementMessage);
		} else {
			log.error("알 수 없는 상태: 주문 ID = {}, 상품 ID = {}, 상태 = {}",
				stockDecrementMessage.getOrderId(), stockDecrementMessage.getProductId(),
				stockDecrementMessage.getStatus());
		}
	}

	@RabbitListener(queues = "${shippingMessage.queue.shipping.response}")
	public void handlerShippingCreateResponse(ShippingResponseMessage shippingResponseMessage) {
		if (shippingResponseMessage.getStatus().equals("SUCCESS")) {
			log.info("배송 생성 성공: 주문 ID = {}, 배송 ID = {}",
				shippingResponseMessage.getOrderId(), shippingResponseMessage.getShippingId());
			orderEventService.updateOrderStatusToWaitingForShipment(shippingResponseMessage);
		} else if (shippingResponseMessage.getStatus().equals("FAIL")) {
			log.error("배송 생성 실패: 주문 ID = {}",
				shippingResponseMessage.getOrderId());
			orderEventService.retryCreateShipping(shippingResponseMessage);
		} else {
			log.error("알 수 없는 상태: 주문 ID = {}, 상태 = {}",
				shippingResponseMessage.getOrderId(), shippingResponseMessage.getStatus());
		}
	}

	@RabbitListener(queues = "${shippingMessage.queue.shipping_update.response}")
	public void handlerShippingUpdateResponse(ShippingResponseMessage shippingResponseMessage) {
		if (shippingResponseMessage.getStatus().equals("SUCCESS")) {
			log.info("배송 업데이트 성공: 주문 ID = {}, 배송 ID = {}",
				shippingResponseMessage.getOrderId(), shippingResponseMessage.getShippingId());
		} else if (shippingResponseMessage.getStatus().equals("FAIL")) {
			log.error("배송 업데이트 실패: 주문 ID = {}, 배송 ID = {}",
				shippingResponseMessage.getOrderId(), shippingResponseMessage.getShippingId());
			orderEventService.retryCreateShipping(shippingResponseMessage);
		} else {
			log.error("알 수 없는 상태: 주문 ID = {}, 배송 ID = {}, 상태 = {}",
				shippingResponseMessage.getOrderId(), shippingResponseMessage.getShippingId(),
				shippingResponseMessage.getStatus());
		}
	}

}
