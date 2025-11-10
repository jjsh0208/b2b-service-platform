package com.devsquad10.shipping.domain.enums;

public enum ShippingStatus {
	HUB_WAIT("HUB_WAIT"), // 허브 대기중
	HUB_TRNS("HUB_TRNS"), // 허브 이동중
	HUB_ARV("HUB_ARV"), // 목적지 허브 도착
	COM_TRNS("COM_TRNS"), // 업체 이동중
	DLV_CMP("DLV_CMP"); // 배송완료

	private final String status;

	ShippingStatus(String status) {
		this.status = status;
	}
}
