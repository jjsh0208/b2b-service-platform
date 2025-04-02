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

	// stock 응답 큐에 대한 설정값
	@Value("${stockMessage.queue.stock.response}")
	private String queueResponseStock;

	// 품절 처리 큐에 대한 설정값
	@Value("${stockMessage.queue.stockSoldOut.request}")
	private String queueStockSoldOut;

	private final RabbitTemplate rabbitTemplate;

	/**
	 * 재고 차감 메시지를 전송하는 메서드
	 *
	 * 재고 차감 처리 후, 관련 정보를 포함한 메시지를 RabbitMQ 큐에 전송합니다.
	 * 메시지에는 상품 이름, 공급자 ID, 가격, 차감 상태 등 필요한 정보가 포함됩니다.
	 *
	 * @param message   재고 차감 요청 메시지
	 * @param product   상품 정보
	 * @param status    차감 상태 (성공, 재고 부족 등)
	 */
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

	/**
	 * 품절 처리 메시지를 전송하는 메서드
	 *
	 * 재고가 0이 되어 품절 상태가 되었을 때, 해당 상품의 품절 상태를 메시지로 전달하여
	 * Company 에서 품절 상태를 알 수 있도록 합니다.
	 *
	 * @param product 품절 처리된 상품
	 */
	@Override
	public void sendStockSoldOutMessage(Product product) {
		StockSoldOutMessage stockSoldOutMessage = new StockSoldOutMessage(product.getSupplierId(),
			product.getName(),
			new Date());

		rabbitTemplate.convertAndSend(queueStockSoldOut, stockSoldOutMessage);
	}

}
