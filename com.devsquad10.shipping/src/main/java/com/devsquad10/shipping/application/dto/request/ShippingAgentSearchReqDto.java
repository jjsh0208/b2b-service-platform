package com.devsquad10.shipping.application.dto.request;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import com.devsquad10.shipping.application.dto.enums.ShippingAgentSortOption;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "배송 담당자 검색 요청 DTO")
public class ShippingAgentSearchReqDto {
	@Schema(description = "배송담당자 고유 UUID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	private UUID id;

	@Schema(description = "소속 허브 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	private UUID hubId;

	@Schema(description = "배송 담당자 ID(=사용자 ID)", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	private UUID shippingManagerId;

	@Schema(description = "페이지 번호(0부터 시작)", type = "integer", example = "0")
	@Builder.Default
	private Integer page = 0;

	@Schema(description = "페이지 크기", type = "integer", example = "10")
	@Builder.Default
	private Integer size = 10;

	@Schema(description = "필드 정렬 기준", type = "string", example = "createdAt")
	@Builder.Default
	private ShippingAgentSortOption sortOption = ShippingAgentSortOption.CREATED_AT;

	@Schema(description = "정렬 방식(acs/desc)", type = "string", example = "desc")
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
