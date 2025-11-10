package com.devsquad10.shipping.application.dto.request;

import java.util.Date;
import java.util.UUID;

import com.devsquad10.shipping.domain.enums.ShippingStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "배송 상태 변경 요청 DTO")
public class ShippingUpdateReqDto {
	@Schema(description = "배송 상태", example = "HUB_WAIT")
	private ShippingStatus status;
	@Schema(description = "주문 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	private UUID orderId;
	@Schema(description = "배송지 주소", example = "서울 강남구 테헤란로231")
	private String address;
	@Schema(description = "요청사항", example = "배송 전 연락 주세요.")
	private String requestDetails;
	@Schema(description = "수령인", example = "김고객")
	private String recipientName;
	@Schema(description = "수령인 슬랙 ID", example = "slack0123")
	private String recipientSlackId;
	@Schema(description = "업체 배송담당자 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	private UUID companyShippingManagerId;
	@Schema(description = "납품 마감일자", example = "2025-03-26")
	private Date deadLine;
}
