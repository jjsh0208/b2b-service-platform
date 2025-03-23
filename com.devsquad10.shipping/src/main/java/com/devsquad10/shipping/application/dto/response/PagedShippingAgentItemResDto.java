package com.devsquad10.shipping.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.devsquad10.shipping.domain.enums.ShippingAgentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Pagination
 * ShippingAgent(배송담당자) 목록의 개별 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedShippingAgentItemResDto {
	private UUID id;
	private UUID hubId;
	private UUID shippingManagerId;
	private String shippingManagerSlackId;
	private ShippingAgentType type;
	private Integer shippingSequence;
	private Boolean isTransit;
	private Integer assignmentCount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
