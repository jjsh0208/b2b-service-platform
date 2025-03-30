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

	/**
	 * 재고 차감 처리
	 *
	 * 주문에 대한 재고 차감을 처리하는 메서드로, 비관적 락을 사용하여 해당 상품의 재고를 차감한다.
	 * 1. 상품 정보를 조회할 때 비관적 락을 적용하여 다른 트랜잭션이 해당 상품의 재고를 변경하지 않도록 한다.
	 * 2. 재고가 부족한 경우, 재고 차감을 수행하지 않고 "재고 부족" 메시지를 전송한다.
	 * 3. 재고가 충분한 경우, 재고를 차감하고, 만약 재고가 0이 되면 "품절" 상태로 변경한다.
	 * 4. 차감 메시지와 품절 메시지를 메시징 시스템에 전송한다.
	 *
	 * @param stockDecrementMessage 재고 차감 요청 메시지
	 * @throws ProductNotFoundException 상품이 존재하지 않을 경우 예외 발생
	 */
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

	/**
	 * 재고 복원 처리
	 *
	 * 주문 취소나 다른 이유로 상품의 재고를 복원한다.
	 * 1. 주어진 상품 ID로 상품을 조회하고, 해당 상품이 존재하는지 확인.
	 * 2. 상품의 재고를 복원 수량만큼 증가시킨다.
	 * 3. 재고 복원 후 메시지를 기록한다.
	 *
	 * @param stockReversalMessage 재고 복원 요청 메시지
	 * @throws ProductNotFoundException 상품이 존재하지 않을 경우 예외 발생
	 */
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
