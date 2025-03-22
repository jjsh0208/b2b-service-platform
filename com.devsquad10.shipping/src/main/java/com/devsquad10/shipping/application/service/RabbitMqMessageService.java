package com.devsquad10.shipping.application.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.devsquad10.shipping.application.dto.message.ShippingCreateResponse;
import com.devsquad10.shipping.application.service.message.ShippingMessageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RabbitMqMessageService implements ShippingMessageService {
	private final RabbitTemplate rabbitTemplate;

	@Value("${shippingMessage.exchange.shipping.response}")
	private String queueShippingCreateResponse;

	@Override
	public void sendShippingCreateMessage(ShippingCreateResponse shippingCreateResponse) {
		rabbitTemplate.convertAndSend(queueShippingCreateResponse, shippingCreateResponse);
	}
	@Override
	public void sendShippingCreateRollbackMessage(ShippingCreateResponse shippingCreateResponse) {
		rabbitTemplate.convertAndSend(queueShippingCreateResponse, shippingCreateResponse);
	}
}
