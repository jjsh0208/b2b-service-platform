package com.devsquad10.hub.application.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubUpdateRequestDto {
	@NotBlank
	private String name;

	@NotBlank
	private String address;

	@NotNull
	private Double latitude;

	@NotNull
	private Double longitude;
}
