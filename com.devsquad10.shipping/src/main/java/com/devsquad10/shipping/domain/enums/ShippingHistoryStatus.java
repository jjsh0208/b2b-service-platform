package com.devsquad10.shipping.domain.enums;

public enum ShippingHistoryStatus {
	HUB_WAIT("HUB_WAIT"), // 허브 이동 대기중
	HUB_TRNS("HUB_TRNS"), // 허브 이동중
	HUB_ARV("HUB_ARV"), // 목적지 허브 도착
	DLV_CMP("TRNS"); // 배송중

	// 최종허브 -> 도착업체 배송 경로 기록을 현재 상태(업체 이동중/배송완료)로 추척
	private final String historyStatus;

	ShippingHistoryStatus(String historyStatus) {
		this.historyStatus = historyStatus;
	}
}
