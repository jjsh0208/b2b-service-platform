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
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductEventService {

	private final ProductRepository productRepository;
	private final ProductMessageService productMessageService;

	public void decreaseStock(StockDecrementMessage stockDecrementMessage) {
		UUID targetProductId = stockDecrementMessage.getProductId();
		int orderQuantity = stockDecrementMessage.getQuantity();

		Product product = productRepository.findByIdWithLock(targetProductId)
			.orElseThrow(() -> {
				log.error("재고 차감 실패 - 상품 ID: {}를 찾을 수 없습니다.", targetProductId);
				return new ProductNotFoundException("Product Not Found By Id :" + targetProductId);
			});

		// 2. 재고 부족 처리
		if (product.getQuantity() < orderQuantity) {
			log.warn("재고 부족 - 상품 ID: {}, 현재 재고: {}, 요청 수량: {}", targetProductId, product.getQuantity(), orderQuantity);
			productMessageService.sendStockDecrementMessage(stockDecrementMessage, product, "OUT_OF_STOCK");
			return;
		}

		product.decreaseStock(orderQuantity);
		log.info("재고 차감 완료 - 상품 ID: {}, 차감 후 재고: {}", targetProductId, product.getQuantity());

		if (product.getQuantity() == 0) {
			product.statusSoldOut();
			productRepository.save(product);
			log.info("품절 처리 완료 - 상품 ID: {}", targetProductId);

			productMessageService.sendStockSoldOutMessage(product);
			log.info("재고 차감 메시지 전송 - 상품 ID: {}, 상태: SUCCESS", targetProductId);
		}

		productMessageService.sendStockDecrementMessage(stockDecrementMessage, product, "SUCCESS");
	}

	public void recoveryStock(StockReversalMessage stockReversalMessage) {

		UUID productId = stockReversalMessage.getProductId();
		int recoveryQuantity = stockReversalMessage.getQuantity();

		Product recoveryProduct = productRepository.findByIdAndDeletedAtIsNull(productId)
			.orElseThrow(() -> {
				log.error("재고 복원 실패 - 상품 ID: {}를 찾을 수 없습니다.", productId);
				return new ProductNotFoundException("Product Not Found By Id :" + productId);
			});

		productRepository.save(recoveryProduct.toBuilder()
			.quantity(recoveryProduct.getQuantity() + recoveryQuantity)
			.build());

		log.info("재고 복원 완료 - 상품 ID: {}, 복원 후 재고: {}", productId, recoveryProduct.getQuantity());
	}
}
