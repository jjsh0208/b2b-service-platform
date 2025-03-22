package com.devsquad10.order.infrastructure.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderApplicationQueueConfig {

	// exchange
	@Value("${stockMessage.exchange.stock.request}")
	private String stockRequestExchange;

	@Value("${stockMessage.exchange.stock.response}")
	private String stockResponseExchange;

	@Value("${stockMessage.exchange.stockRecovery.request}")
	private String stockRecoveryExchange;

	@Value("${shippingMessage.exchange.shipping.request}")
	private String shippingCreateRequestExchange;

	@Value("${shippingMessage.exchange.shipping.response}")
	private String shippingCreateResponseExchange;

	@Value("${shippingMessage.exchange.shipping_update.request}")
	private String shippingUpdateRequestExchange;

	@Value("${shippingMessage.exchange.shipping_update.response}")
	private String shippingUpdateResponseExchange;

	//queue
	@Value("${stockMessage.queue.stock.request}")
	private String queueRequestStock;

	@Value("${stockMessage.queue.stock.response}")
	private String queueResponseStock;

	@Value("${stockMessage.queue.stockRecovery.request}")
	private String queueStockRecovery;

	@Value("${shippingMessage.queue.shipping.request}")
	private String queueShippingCreateRequest;

	@Value("${shippingMessage.queue.shipping.response}")
	private String queueShippingCreateResponse;

	@Value("${shippingMessage.queue.shipping_update.request}")
	private String queueShippingUpdateRequest;

	@Value("${shippingMessage.queue.shipping_update.response}")
	private String queueShippingUpdateResponse;

	/**
	 * exchange
	 */
	@Bean
	public TopicExchange stockRequestExchange() {
		return new TopicExchange(stockRequestExchange);
	}

	@Bean
	public TopicExchange stockResponseExchange() {
		return new TopicExchange(stockResponseExchange);
	}

	@Bean
	public TopicExchange stockRecoveryExchange() {
		return new TopicExchange(stockRecoveryExchange);
	}

	@Bean
	public TopicExchange shippingCreateRequestExchange() {
		return new TopicExchange(shippingCreateRequestExchange);
	}

	@Bean
	public TopicExchange shippingCreateResponseExchange() {
		return new TopicExchange(shippingCreateResponseExchange);
	}

	@Bean
	public TopicExchange shippingUpdateRequestExchange() {
		return new TopicExchange(shippingUpdateRequestExchange);
	}

	@Bean
	public TopicExchange shippingUpdateResponseExchange() {
		return new TopicExchange(shippingUpdateResponseExchange);
	}

	/**
	 * queue
	 */
	@Bean
	public Queue queueRequestStock() {
		return new Queue(queueRequestStock);
	}

	@Bean
	public Queue queueResponseStock() {
		return new Queue(queueResponseStock);
	}

	@Bean
	public Queue queueStockRecovery() {
		return new Queue(queueStockRecovery);
	}

	@Bean
	public Queue queueShippingCreateRequest() {
		return new Queue(queueShippingCreateRequest);
	}

	@Bean
	public Queue queueShippingCreateResponse() {
		return new Queue(queueShippingCreateResponse);
	}

	@Bean
	public Queue queueShippingUpdateRequest() {
		return new Queue(queueShippingUpdateRequest);
	}

	@Bean
	public Queue queueShippingUpdateResponse() {
		return new Queue(queueShippingUpdateResponse);
	}

	/**
	 * binding
	 */
	@Bean
	public Binding bindingRequestStock() {
		return BindingBuilder.bind(queueRequestStock()).to(stockRequestExchange()).with(queueRequestStock);
	}

	@Bean
	public Binding bindingResponseStock() {
		return BindingBuilder.bind(queueResponseStock()).to(stockResponseExchange()).with(queueResponseStock);
	}

	@Bean
	public Binding bindingRecoveryStock() {
		return BindingBuilder.bind(queueStockRecovery()).to(stockRecoveryExchange()).with(queueStockRecovery);
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
	public Binding bindingRequestShippingUpdate() {
		return BindingBuilder.bind(queueShippingUpdateRequest())
			.to(shippingUpdateRequestExchange())
			.with(queueShippingUpdateRequest);
	}

	@Bean
	public Binding bindingResponseShippingUpdate() {
		return BindingBuilder.bind(queueShippingUpdateResponse())
			.to(shippingUpdateResponseExchange())
			.with(queueShippingUpdateResponse);
	}

	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
