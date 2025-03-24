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
 * Pagination
 * Shipping(배송) 목록의 개별 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedShippingItemResDto implements Serializable {
	private UUID id;
	private ShippingStatus status;
	private UUID departureHubId;
	private UUID destinationHubId;
	private UUID orderId;
	private String address;
	private String recipientName;
	private String recipientSlackId;
	private String requestDetails;
	private Date deadLine;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
