package com.devsquad10.product.application.service;

import java.util.Date;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.devsquad10.product.application.dto.message.StockDecrementMessage;
import com.devsquad10.product.application.dto.message.StockSoldOutMessage;
import com.devsquad10.product.application.messaging.ProductMessageService;
import com.devsquad10.product.domain.model.Product;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductMessageServiceImpl implements ProductMessageService {

	@Value("${stockMessage.queue.stock.response}")
	private String queueResponseStock;

	@Value("${stockMessage.queue.stockSoldOut.request}")
	private String queueStockSoldOut;

	private final RabbitTemplate rabbitTemplate;

	@Override
	public void sendStockDecrementMessage(StockDecrementMessage message, Product product, String status) {
		StockDecrementMessage updatedMessage = message.toBuilder()
			.productName(product.getName())
			.supplierId(product.getSupplierId())
			.price(product.getPrice())
			.status(status)
			.build();

		rabbitTemplate.convertAndSend(queueResponseStock, updatedMessage);
	}

	@Override
	public void sendStockSoldOutMessage(Product product) {
		StockSoldOutMessage stockSoldOutMessage = new StockSoldOutMessage(product.getSupplierId(),
			product.getName(),
			new Date());

		rabbitTemplate.convertAndSend(queueStockSoldOut, stockSoldOutMessage);
	}

}
