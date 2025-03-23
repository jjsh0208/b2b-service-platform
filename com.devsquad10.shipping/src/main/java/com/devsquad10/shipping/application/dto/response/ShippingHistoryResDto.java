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
 * ShippingHistory(배송경로기록) 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingHistoryResDto implements Serializable {
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
	private UUID createdBy;
	private LocalDateTime updatedAt;
	private UUID updatedBy;
	private LocalDateTime deletedAt;
	private UUID deletedBy;
}
