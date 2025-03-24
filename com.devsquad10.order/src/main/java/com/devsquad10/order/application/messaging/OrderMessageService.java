package com.devsquad10.order.application.messaging;

import com.devsquad10.order.application.dto.message.ShippingCreateRequest;
import com.devsquad10.order.application.dto.message.ShippingUpdateRequest;
import com.devsquad10.order.application.dto.message.StockDecrementMessage;
import com.devsquad10.order.application.dto.message.StockReversalMessage;

public interface OrderMessageService {
	void sendStockDecrementMessage(StockDecrementMessage stockDecrementMessage);

	void sendStockReversalMessage(StockReversalMessage stockReversalMessage);

	void sendShippingCreateMessage(ShippingCreateRequest shippingCreateRequest);

	void sendShippingUpdateMessage(ShippingUpdateRequest shippingUpdateRequest);
}
