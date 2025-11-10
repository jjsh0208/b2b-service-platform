package com.devsquad10.order.application.dto;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderUpdateReqDto {

	private UUID recipientsId;

	private Integer quantity; // 주문 수량

	private String requestDetails; // 요청사항

	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date deadLine; // 납품기한일자
}
