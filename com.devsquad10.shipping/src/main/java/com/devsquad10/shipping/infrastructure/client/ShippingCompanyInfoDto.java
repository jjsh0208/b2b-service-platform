package com.devsquad10.shipping.infrastructure.client;

import java.util.UUID;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ShippingCompanyInfoDto {

	@Column
	private UUID venderId;

	@Column
	private UUID hubId;
}