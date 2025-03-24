package com.devsquad10.shipping.application.dto.request;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.devsquad10.shipping.application.dto.enums.ShippingSortOption;
import com.devsquad10.shipping.domain.enums.ShippingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Shipping(배송)조회 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingSearchReqDto {
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

	@Builder.Default
	private Integer page = 0;

	@Builder.Default
	private Integer size = 10;

	@Builder.Default
	private ShippingSortOption sortOption = ShippingSortOption.CREATED_AT;

	@Builder.Default
	private Sort.Direction sortOrder = Sort.Direction.DESC;

	public int getPage() {
		return (page != null && page > 0) ? page - 1 : 0;
	}

	public int getSize() {
		return Optional.ofNullable(size)
			.filter(s -> Set.of(10, 30, 50).contains(s))
			.orElse(10);
	}
}
