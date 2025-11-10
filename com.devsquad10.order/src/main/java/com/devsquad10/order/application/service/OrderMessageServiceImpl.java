package com.devsquad10.order.application.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.devsquad10.order.application.dto.message.ShippingCreateRequest;
import com.devsquad10.order.application.dto.message.ShippingUpdateRequest;
import com.devsquad10.order.application.dto.message.StockDecrementMessage;
import com.devsquad10.order.application.dto.message.StockReversalMessage;
import com.devsquad10.order.application.messaging.OrderMessageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderMessageServiceImpl implements OrderMessageService {
	private final RabbitTemplate rabbitTemplate;

	// 재고 감소 요청 큐에 대한 설정값
	@Value("${stockMessage.queue.stock.request}")
	private String queueRequestStock;

	// 재고 복원 요청 큐에 대한 설정값
	@Value("${stockMessage.queue.stockRecovery.request}")
	private String queueStockRecovery;

	// 배송 생성 요청 큐에 대한 설정값
	@Value("${shippingMessage.queue.shipping.request}")
	private String queueShippingCreateRequest;

	// 배송 수정 요청 큐에 대한 설정값
	@Value("${shippingMessage.queue.shipping_update.request}")
	private String queueShippingUpdateRequest;

	@Override
	public void sendStockDecrementMessage(StockDecrementMessage stockDecrementMessage) {
		rabbitTemplate.convertAndSend(queueRequestStock, stockDecrementMessage);
	}

	@Override
	public void sendStockReversalMessage(StockReversalMessage stockReversalMessage) {
		rabbitTemplate.convertAndSend(queueStockRecovery, stockReversalMessage);
	}

	@Override
	public void sendShippingCreateMessage(ShippingCreateRequest shippingCreateRequest) {
		rabbitTemplate.convertAndSend(queueShippingCreateRequest, shippingCreateRequest);
	}

	@Override
	public void sendShippingUpdateMessage(ShippingUpdateRequest shippingUpdateRequest) {
		rabbitTemplate.convertAndSend(queueShippingUpdateRequest, shippingUpdateRequest);
	}
}
