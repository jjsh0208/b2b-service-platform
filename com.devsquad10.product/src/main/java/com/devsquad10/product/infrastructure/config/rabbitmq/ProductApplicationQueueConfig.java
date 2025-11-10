package com.devsquad10.product.infrastructure.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductApplicationQueueConfig {

	@Value("${stockMessage.exchange.stock.soldOut}")
	private String stockSoldOutRequestExchange;

	@Value("${stockMessage.queue.stockSoldOut.request}")
	private String queueStockSoldOut;

	@Bean
	public TopicExchange stockSoldOutRequestExchange() {
		return new TopicExchange(stockSoldOutRequestExchange);
	}

	@Bean
	public Queue queueStockSoldOut() {
		return new Queue(queueStockSoldOut);
	}

	@Bean
	public Binding bindingStockSoldOut() {
		return BindingBuilder.bind(queueStockSoldOut()).to(stockSoldOutRequestExchange()).with(queueStockSoldOut);
	}

	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
