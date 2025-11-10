package com.devsquad10.order.application.dto.message;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDecrementMessage {
	private UUID orderId;

	private UUID productId;

	private UUID supplierId;

	private String productName;

	private Integer quantity;

	private String status;

	private Integer price;
}
