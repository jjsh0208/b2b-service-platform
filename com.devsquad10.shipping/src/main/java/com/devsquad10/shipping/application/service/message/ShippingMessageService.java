package com.devsquad10.shipping.application.service.message;

import com.devsquad10.shipping.application.dto.message.ShippingCreateResponse;

public interface ShippingMessageService {
	// 배송 생성 후, 주문에 메시지 전달(주문id, 배송id, 상태)
	void sendShippingCreateMessage(ShippingCreateResponse shippingCreateResponse);

	// 배송 생성 실패시 보상 트랜잭션 메시지 전달
	void sendShippingCreateRollbackMessage(ShippingCreateResponse rollbackMessage);
}
