package com.devsquad10.product.application.endpoint;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.devsquad10.product.application.dto.message.StockDecrementMessage;
import com.devsquad10.product.application.dto.message.StockReversalMessage;
import com.devsquad10.product.application.service.ProductEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductEndPoint {

	private final ProductEventService productEventService;

	@RabbitListener(queues = "${stockMessage.queue.stock.request}", concurrency = "1")
	public void handleStockDecrementRequest(StockDecrementMessage stockDecrementMessage) {
		log.info("재고 차감 요청 수신 - 상품 ID: {}, 차감 수량: {}", stockDecrementMessage.getProductId(),
			stockDecrementMessage.getQuantity());
		productEventService.decreaseStock(stockDecrementMessage);
	}

	@RabbitListener(queues = "${stockMessage.queue.stockRecovery.request}", concurrency = "1")
	public void handlerStockRecoveryRequest(StockReversalMessage stockReversalMessage) {
		log.info("재고 복원 요청 수신 - 상품 ID: {}, 복원 수량: {}", stockReversalMessage.getProductId(),
			stockReversalMessage.getQuantity());
		productEventService.recoveryStock(stockReversalMessage);
	}
}
