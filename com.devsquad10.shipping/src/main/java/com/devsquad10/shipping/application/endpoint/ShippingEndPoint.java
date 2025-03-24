package com.devsquad10.shipping.application.endpoint;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.devsquad10.shipping.application.dto.message.ShippingCreateMessage;
import com.devsquad10.shipping.application.dto.message.ShippingUpdateMessage;
import com.devsquad10.shipping.application.service.ShippingEventService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class ShippingEndPoint {

	private final ShippingEventService shippingEventService;

	@RabbitListener(queues = "${shippingMessage.queue.shipping.request}", concurrency = "1")
	public void handlerShippingCreateRequest(ShippingCreateMessage shippingCreateMessage) {
		try {
			shippingEventService.handlerShippingCreateRequest(shippingCreateMessage);
		} catch (JsonProcessingException e) {
			log.error("배송 생성을 위한 RabbitMQ 메시지 처리 실패: " + e.getMessage());
			throw new RuntimeException("배송 생성을 위한 RabbitMQ 메시지 처리 실패: " + e.getMessage(), e);
		}
	}

	@RabbitListener(queues = "${shippingMessage.queue.shipping_update.request}", concurrency = "1")
	public void updateOrderAndShippingDetails(ShippingUpdateMessage shippingUpdateMessage) {
		shippingEventService.handlerShippingUpdateRequest(shippingUpdateMessage);
	}
}
