package com.devsquad10.product.application.messaging;

import com.devsquad10.product.application.dto.message.StockDecrementMessage;
import com.devsquad10.product.domain.model.Product;

public interface ProductMessageService {

	void sendStockDecrementMessage(StockDecrementMessage message, Product product, String status); // 재고 감소 메시지

	void sendStockSoldOutMessage(Product product); // 재고 부족 메시지
}
