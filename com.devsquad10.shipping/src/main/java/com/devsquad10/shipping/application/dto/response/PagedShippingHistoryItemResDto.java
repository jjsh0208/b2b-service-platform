package com.devsquad10.shipping.application.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.devsquad10.shipping.domain.enums.ShippingHistoryStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Pagination
 * ShippingHistory(배송경로기록) 목록의 개별 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedShippingHistoryItemResDto implements Serializable {
	private UUID id;
	private Integer shippingPathSequence;
	private UUID departureHubId;
	private UUID destinationHubId;
	private UUID shippingManagerId;
	private Double estDist;
	private Integer estTime;
	private Double actDist;
	private Integer actTime;
	private ShippingHistoryStatus historyStatus;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
