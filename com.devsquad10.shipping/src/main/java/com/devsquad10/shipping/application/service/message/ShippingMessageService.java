package com.devsquad10.shipping.application.service.message;

import com.devsquad10.shipping.application.dto.message.ShippingResponseMessage;
import com.devsquad10.shipping.application.dto.message.ShippingUpdateMessage;

public interface ShippingMessageService {
	// 배송 생성 후, 주문에 메시지 발행(주문id, 배송id, 상태)
	void sendShippingCreateMessage(ShippingResponseMessage shippingResponseMessage);

	// 배송 생성 실패시 보상 트랜잭션 메시지 발행
	void sendShippingCreateRollbackMessage(ShippingResponseMessage rollbackMessage);

	// 배송 수정 후, 주문에 메시지 발행(주문id, 배송id, 상태)
	void sendUpdateOrderAndShippingDetails(ShippingResponseMessage shippingResponseMessage);

	// 배송 수정 실패 시, 주문에 보상 트랜잭션 메시지 발행
	void sendUpdateOrderAndShippingDetailsRollbackMessage(ShippingResponseMessage rollbackMessage);
}
