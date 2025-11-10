package com.devsquad10.hub.infrastructure.client.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NaverDirections5Response {
	private Integer distance;
	private Integer duration;
}
