package com.devsquad10.shipping.infrastructure.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShippingApplicationQueueConfig {

	@Value("${shippingMessage.exchange.shipping.request}")
	private String shippingCreateRequestExchange;

	@Value("${shippingMessage.exchange.shipping.response}")
	private String shippingCreateResponseExchange;

	@Value("${shippingMessage.queue.shipping.request}")
	private String queueShippingCreateRequest;

	@Value("${shippingMessage.queue.shipping.response}")
	private String queueShippingCreateResponse;

	@Bean
	public TopicExchange shippingCreateRequestExchange() {
		return new TopicExchange(shippingCreateRequestExchange);
	}

	@Bean
	public TopicExchange shippingCreateResponseExchange() {
		return new TopicExchange(shippingCreateResponseExchange);
	}

	@Bean
	public Queue queueShippingCreateRequest() {	return new Queue(queueShippingCreateRequest); }

	@Bean
	public Queue queueShippingCreateResponse() {
		return new Queue(queueShippingCreateResponse);
	}

	@Bean
	public Binding bindingRequestShipping() {
		return BindingBuilder.bind(queueShippingCreateRequest())
			.to(shippingCreateRequestExchange())
			.with(queueShippingCreateRequest);
	}

	@Bean
	public Binding bindingResponseShipping() {
		return BindingBuilder.bind(queueShippingCreateResponse())
			.to(shippingCreateResponseExchange())
			.with(queueShippingCreateResponse);
	}

	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}