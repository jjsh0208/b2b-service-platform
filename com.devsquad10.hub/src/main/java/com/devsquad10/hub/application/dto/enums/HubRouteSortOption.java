package com.devsquad10.hub.application.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HubRouteSortOption {
	CREATED_AT,
	UPDATED_AT,
	DISTANCE,
	DURATION
}
