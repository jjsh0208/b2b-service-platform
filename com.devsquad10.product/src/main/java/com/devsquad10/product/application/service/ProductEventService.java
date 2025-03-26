package com.devsquad10.product.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.product.application.dto.message.StockDecrementMessage;
import com.devsquad10.product.application.dto.message.StockReversalMessage;
import com.devsquad10.product.application.exception.ProductNotFoundException;
import com.devsquad10.product.application.messaging.ProductMessageService;
import com.devsquad10.product.domain.model.Product;
import com.devsquad10.product.domain.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductEventService {

	private final ProductRepository productRepository;
	private final ProductMessageService productMessageService;

	public void decreaseStock(StockDecrementMessage stockDecrementMessage) {
		UUID targetProductId = stockDecrementMessage.getProductId();
		int orderQuantity = stockDecrementMessage.getQuantity();

		Product product = productRepository.findByIdWithLock(targetProductId)
			.orElseThrow(() -> new ProductNotFoundException("Product Not Found By Id :" + targetProductId));

		// 2. 재고 부족 처리
		if (product.getQuantity() < orderQuantity) {
			productMessageService.sendStockDecrementMessage(stockDecrementMessage, product, "OUT_OF_STOCK");
			return;
		}

		product.decreaseStock(orderQuantity);

		if (product.getQuantity() == 0) {
			product.statusSoldOut();
			productRepository.save(product);

			productMessageService.sendStockSoldOutMessage(product);
		}

		productMessageService.sendStockDecrementMessage(stockDecrementMessage, product, "SUCCESS");
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
