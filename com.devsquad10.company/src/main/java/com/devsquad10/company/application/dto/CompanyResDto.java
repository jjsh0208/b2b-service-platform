package com.devsquad10.company.application.dto;

import java.io.Serializable;
import java.util.UUID;

import com.devsquad10.company.domain.enums.CompanyTypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CompanyResDto implements Serializable {

	private UUID id;

	private String name;

	private UUID venderId;

	private UUID hubId;

	private String address;

	private CompanyTypes type;
}
