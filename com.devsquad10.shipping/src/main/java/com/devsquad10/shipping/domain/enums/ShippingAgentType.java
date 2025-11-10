package com.devsquad10.shipping.domain.enums;

public enum ShippingAgentType {
	HUB_DVL("HUB_DVL"), // 허브 배송 담당자 - 전체 허브 10명
	COM_DVL("COM_DVL"); // 업체 배송 담당자 - 허브별 10명(허브 17곳, 총 170명)

	private final String type;

	ShippingAgentType(String type) {
		this.type = type;
	}
}
