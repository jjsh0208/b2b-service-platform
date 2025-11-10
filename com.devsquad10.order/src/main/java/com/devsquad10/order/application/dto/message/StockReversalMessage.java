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
public class StockReversalMessage {

	private UUID productId;
	private int quantity;
}
