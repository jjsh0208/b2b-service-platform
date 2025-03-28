package com.devsquad10.company.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SoldOutMessageRequest {

	private String venderSlackId;

	private String productName;

	private String soldOutAt;  // 0000-00-00

}
