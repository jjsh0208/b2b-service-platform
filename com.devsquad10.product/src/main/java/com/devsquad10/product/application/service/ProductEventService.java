package com.devsquad10.product.application.service;

import java.util.Date;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.product.application.dto.message.StockDecrementMessage;
import com.devsquad10.product.application.dto.message.StockReversalMessage;
import com.devsquad10.product.application.dto.message.StockSoldOutMessage;
import com.devsquad10.product.application.exception.ProductNotFoundException;
import com.devsquad10.product.domain.model.Product;
import com.devsquad10.product.domain.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductEventService {

	@Value("${stockMessage.queue.stock.response}")
	private String queueResponseStock;

	@Value("${stockMessage.queue.stockSoldOut.request}")
	private String queueStockSoldOut;

	private final ProductRepository productRepository;
	private final RabbitTemplate rabbitTemplate;

	public void decreaseStock(StockDecrementMessage stockDecrementMessage) {
		UUID targetProductId = stockDecrementMessage.getProductId();
		int orderQuantity = stockDecrementMessage.getQuantity();

		Product product = productRepository.findByIdWithLock(targetProductId)
			.orElseThrow(() -> new ProductNotFoundException("Product Not Found By Id :" + targetProductId));

		// 2. 재고 부족 처리
		if (product.getQuantity() < orderQuantity) {
			sendStockUpdateMessage(stockDecrementMessage, product, "OUT_OF_STOCK");
			return;
		}

		product.decreaseStock(orderQuantity);

		if (product.getQuantity() == 0) {
			product.statusSoldOut();
			productRepository.save(product);

			StockSoldOutMessage stockSoldOutMessage = new StockSoldOutMessage(product.getSupplierId(), product.getId(),
				new Date());
			rabbitTemplate.convertAndSend(queueStockSoldOut, stockSoldOutMessage);
		}

		sendStockUpdateMessage(stockDecrementMessage, product, "SUCCESS");
	}

	private void sendStockUpdateMessage(StockDecrementMessage message, Product product, String status) {
		StockDecrementMessage updatedMessage = message.toBuilder()
			.productName(product.getName())
			.supplierId(product.getSupplierId())
			.price(product.getPrice())
			.status(status)
			.build();

		rabbitTemplate.convertAndSend(queueResponseStock, updatedMessage);
	}

	public void recoveryStock(StockReversalMessage stockReversalMessage) {

		UUID productId = stockReversalMessage.getProductId();
		int recoveryQuantity = stockReversalMessage.getQuantity();

		Product recoveryProduct = productRepository.findByIdAndDeletedAtIsNull(productId)
			.orElseThrow(
				() -> new ProductNotFoundException("Product Not Found By Id :" + productId));

		productRepository.save(recoveryProduct.toBuilder()
			.quantity(recoveryProduct.getQuantity() + recoveryQuantity)
			.build());
	}
}
