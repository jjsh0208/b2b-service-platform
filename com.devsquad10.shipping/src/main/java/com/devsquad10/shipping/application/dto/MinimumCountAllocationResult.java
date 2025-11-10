package com.devsquad10.shipping.application.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MinimumCountAllocationResult {
	private UUID hubId;
	private UUID shippingManagerId;
	private String shippingManagerSlackId;
	private Integer shippingSequence;
	private Boolean isTransit;
	private Integer assignmentCount;
}
