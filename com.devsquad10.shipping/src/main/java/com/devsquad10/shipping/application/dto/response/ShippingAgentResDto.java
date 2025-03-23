package com.devsquad10.shipping.application.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.devsquad10.shipping.domain.enums.ShippingAgentType;
import com.devsquad10.shipping.domain.model.ShippingAgent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ShippingAgent(배송담당자) 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingAgentResDto implements Serializable {
	private UUID id;
	private UUID hubId;
	private UUID shippingManagerId;
	private String shippingManagerSlackId;
	private ShippingAgentType type;
	private Integer shippingSequence;
	private Boolean isTransit;
	private Integer assignmentCount;
	private LocalDateTime createdAt;
	private UUID createdBy;
	private LocalDateTime updatedAt;
	private UUID updatedBy;
	private LocalDateTime deletedAt;
	private UUID deletedBy;

	public static ShippingAgentResDto toResponse(ShippingAgent shippingAgent) {
		return ShippingAgentResDto.builder()
			.id(shippingAgent.getId())
			.hubId(shippingAgent.getHubId())
			.shippingManagerId(shippingAgent.getShippingManagerId())
			.shippingManagerSlackId(shippingAgent.getShippingManagerSlackId())
			.shippingSequence(shippingAgent.getShippingSequence())
			.type(shippingAgent.getType())
			.isTransit(shippingAgent.getIsTransit())
			.assignmentCount(shippingAgent.getAssignmentCount())
			.build();
	}
}
