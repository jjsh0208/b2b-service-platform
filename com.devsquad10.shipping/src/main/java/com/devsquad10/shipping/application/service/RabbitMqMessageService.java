package com.devsquad10.shipping.application.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.devsquad10.shipping.application.dto.message.ShippingResponseMessage;
import com.devsquad10.shipping.application.service.message.ShippingMessageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RabbitMqMessageService implements ShippingMessageService {
	private final RabbitTemplate rabbitTemplate;

	@Value("${shippingMessage.queue.shipping.response}")
	private String queueShippingCreateResponse;

	@Value("${shippingMessage.queue.shipping_update.response}")
	private String queueShippingUpdateResponse;

	@Override
	public void sendShippingCreateMessage(ShippingResponseMessage shippingResponseMessage) {
		rabbitTemplate.convertAndSend(queueShippingCreateResponse, shippingResponseMessage);
	}
	@Override
	public void sendShippingCreateRollbackMessage(ShippingResponseMessage shippingResponseMessage) {
		rabbitTemplate.convertAndSend(queueShippingCreateResponse, shippingResponseMessage);
	}
	@Override
	public void sendUpdateOrderAndShippingDetails(ShippingResponseMessage shippingResponseMessage) {
		rabbitTemplate.convertAndSend(queueShippingUpdateResponse, shippingResponseMessage);
	}
	@Override
	public void sendUpdateOrderAndShippingDetailsRollbackMessage(ShippingResponseMessage rollbackMessage) {
		rabbitTemplate.convertAndSend(queueShippingUpdateResponse, rollbackMessage);
	}
}
