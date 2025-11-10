package com.devsquad10.shipping.application.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import com.devsquad10.shipping.domain.enums.ShippingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Shipping(배송) 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingResDto implements Serializable {
	private UUID id;
	private ShippingStatus status;
	private UUID departureHubId;
	private UUID destinationHubId;
	private UUID orderId;
	private String address;
	private String recipientName;
	private String recipientSlackId;
	private String requestDetails;
	private UUID companyShippingManagerId;
	private Date deadLine;
	private LocalDateTime createdAt;
	private UUID createdBy;
	private LocalDateTime updatedAt;
	private UUID updatedBy;
	private LocalDateTime deletedAt;
	private UUID deletedBy;
}
