package com.devsquad10.hub.application.dto.res;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.devsquad10.hub.domain.model.Hub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HubUpdateResponseDto implements Serializable {

	// TODO: dto 개선

	private UUID id;
	private String name;
	private String address;
	private Double latitude;
	private Double longitude;
	private LocalDateTime createdAt;
	private UUID createdBy;
	private LocalDateTime updatedAt;
	private UUID updatedBy;
	private LocalDateTime deletedAt;
	private UUID deletedBy;

	public static HubUpdateResponseDto toResponseDto(Hub hub) {
		return HubUpdateResponseDto.builder()
			.id(hub.getId())
			.name(hub.getName())
			.address(hub.getAddress())
			.latitude(hub.getLatitude())
			.longitude(hub.getLongitude())
			.createdAt(hub.getCreatedAt())
			.createdBy(hub.getCreatedBy())
			.updatedAt(hub.getUpdatedAt())
			.updatedBy(hub.getUpdatedBy())
			.deletedAt(hub.getDeletedAt())
			.deletedBy(hub.getDeletedBy())
			.build();
	}
}
