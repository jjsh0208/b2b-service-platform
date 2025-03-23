package com.devsquad10.shipping.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.devsquad10.shipping.domain.enums.ShippingAgentType;
import com.devsquad10.shipping.domain.model.ShippingAgent;

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

	public static PagedShippingAgentItemResDto toResponse(ShippingAgent shippingAgent) {
		return PagedShippingAgentItemResDto.builder()
			.id(shippingAgent.getId())
			.hubId(shippingAgent.getHubId())
			.shippingManagerId(shippingAgent.getShippingManagerId())
			.shippingManagerSlackId(shippingAgent.getShippingManagerSlackId())
			.type(shippingAgent.getType())
			.shippingSequence(shippingAgent.getShippingSequence())
			.isTransit(shippingAgent.getIsTransit())
			.assignmentCount(shippingAgent.getAssignmentCount())
			.createdAt(shippingAgent.getCreatedAt())
			.updatedAt(shippingAgent.getUpdatedAt())
			.build();
	}
}
