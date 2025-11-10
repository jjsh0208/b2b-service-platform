package com.devsquad10.product.application.dto;

import java.io.Serializable;
import java.util.UUID;

import com.devsquad10.product.domain.enums.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProductResDto implements Serializable {

	private UUID id;

	private String name;

	private String description;

	private Integer quantity;

	private Integer price;

	private UUID supplierId;

	private UUID hubId;

	private ProductStatus status;
}
