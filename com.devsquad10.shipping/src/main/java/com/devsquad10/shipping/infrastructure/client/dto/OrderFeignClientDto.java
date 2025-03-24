package com.devsquad10.shipping.infrastructure.client.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderFeignClientDto {
	private String productName; // 상품명
	private Integer quantity; // 수량
}
