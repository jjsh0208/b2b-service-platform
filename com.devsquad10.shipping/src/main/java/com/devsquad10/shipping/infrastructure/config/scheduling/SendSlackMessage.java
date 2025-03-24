package com.devsquad10.shipping.infrastructure.config.scheduling;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.devsquad10.shipping.domain.model.Shipping;
import com.devsquad10.shipping.domain.repository.ShippingRepository;
import com.devsquad10.shipping.infrastructure.client.MessageClient;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SendSlackMessage {

	private final ShippingRepository shippingRepository;
	private final MessageClient messageClient;

	@Scheduled(cron = "0 0 6 * * *")
	public void sendDailySlackNotification(Date deadLine) {
		List<Shipping> dailyDeadLines = shippingRepository.findShippingWithDeadlineToday(deadLine);
		for(Shipping shipping : dailyDeadLines) {
			sendSlackNotification(shipping.getOrderId());
		}
	}

	private void sendSlackNotification(UUID orderId) {
		messageClient.generateAndSendShippingTimeMessage(orderId);
	}
}
