package com.devsquad10.shipping.application.endpoint;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.devsquad10.shipping.application.dto.message.ShippingCreateRequest;
import com.devsquad10.shipping.application.service.ShippingEventService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class ShippingEndPoint {

	private final ShippingEventService shippingEventService;

	@RabbitListener(queues = "${shippingMessage.queue.shipping.request}")
	public void handlerShippingCreateRequest(ShippingCreateRequest shippingCreateRequest) {
		try {
			shippingEventService.handlerShippingCreateRequest(shippingCreateRequest);
		} catch (JsonProcessingException e) {
			log.warn("배송 생성을 위한 RabbitMQ 메시지 처리 실패: " + e.getMessage());
			throw new RuntimeException("배송 생성을 위한 RabbitMQ 메시지 처리 실패: " + e.getMessage(), e);
		}
	}

	// @RabbitListener(queues = "${shippingMessage.queue.order.update}")
	// public void updateOrderStatusAndShippingDetails(ShippingCreateRequest shippingCreateRequest) {
	// 	shippingEventService.handlerOrderUpdateMessage(shippingCreateRequest);
	// }
}
