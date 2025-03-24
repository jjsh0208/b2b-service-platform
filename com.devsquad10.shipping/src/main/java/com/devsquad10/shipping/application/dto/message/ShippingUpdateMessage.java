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
public class ShippingUpdateMessage {
	// 변경될 수 있는 사항은 수령 업체 변경 (주소 변경)
	// 요청사항, 납품 기한 일자 변경 가능

	private UUID orderId; // 주문 ID

	private UUID recipientsId; // 수령업체

	private String address;    //

	private String requestDetails; // 요청사항

	private Date deadLine; // 납품기한일자
}
