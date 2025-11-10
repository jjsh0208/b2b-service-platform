package com.devsquad10.product.domain.enums;

public enum ProductStatus {
	AVAILABLE("AVAILABLE"), // 판매중
	SOLD_OUT("SOLD_OUT"); // sold out

	private final String status;

	ProductStatus(String status) {
		this.status = status;
	}
}
