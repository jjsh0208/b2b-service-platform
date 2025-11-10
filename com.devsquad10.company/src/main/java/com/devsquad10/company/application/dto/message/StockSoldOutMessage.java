package com.devsquad10.company.application.dto.message;

import java.util.Date;
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
public class StockSoldOutMessage {

	private UUID supplierId;

	private String productName;

	private Date soldOutAt;
}