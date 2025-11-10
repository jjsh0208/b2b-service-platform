package com.devsquad10.company.domain.enums;

public enum CompanyTypes {
	SUPPLIER("SUPPLIER"),
	RECIPIENTS("RECIPIENTS");

	private final String type;

	CompanyTypes(String type) {
		this.type = type;
	}
}

