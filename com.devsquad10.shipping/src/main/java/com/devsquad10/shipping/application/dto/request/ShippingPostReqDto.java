package com.devsquad10.shipping.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShippingPostReqDto {

	private String recipientName;

	private String recipientSlackId;
}
