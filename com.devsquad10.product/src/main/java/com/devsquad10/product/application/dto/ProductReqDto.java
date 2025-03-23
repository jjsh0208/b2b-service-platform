package com.devsquad10.product.application.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProductReqDto {

	private String name;

	private String description;

	private Integer quantity;

	private Integer price;

	private UUID supplierId;
	
}
