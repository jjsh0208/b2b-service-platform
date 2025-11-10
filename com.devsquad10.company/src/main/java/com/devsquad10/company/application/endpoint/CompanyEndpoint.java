package com.devsquad10.company.application.endpoint;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.devsquad10.company.application.dto.message.StockSoldOutMessage;
import com.devsquad10.company.application.service.CompanyEventService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CompanyEndpoint {

	private final CompanyEventService companyEventService;

	@RabbitListener(queues = "${stockMessage.queue.stockSoldOut.request}")
	public void handleStockSoldOutMessage(StockSoldOutMessage stockSoldOutMessage) {
		companyEventService.stockSoldMessageSend(stockSoldOutMessage);
	}

}
