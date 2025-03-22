package com.devsquad10.shipping.application.dto.message;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingCreateRequest {
	//배송에 보낼 메시지 생생 (공급업체, 수량업체,  업체 주소, orderId, 요청 사항)
	private UUID orderId;

	private UUID supplierId; // 공급업체

	private UUID recipientsId; // 수령업체

	private String address;

	private String requestDetails; // 요청사항

	private Date deadLine; // 납품기한일자
	// private String deadLine; // 납품기한일자
}
