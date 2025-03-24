package com.devsquad10.shipping.infrastructure.config.scheduling;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.devsquad10.shipping.domain.model.Shipping;
import com.devsquad10.shipping.domain.repository.ShippingRepository;
import com.devsquad10.shipping.infrastructure.client.MessageClient;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class SendSlackMessageScheduler {

	private final ShippingRepository shippingRepository;
	private final MessageClient messageClient;

	// @Scheduled(cron = "0 0 6 * * *") // 매일 오전 6시
	// @Scheduled(cron = "* */2 * * * *") // 테스트용 2분 마다
	public void sendDailySlackNotification() {
		List<Shipping> dailyDeadLines = shippingRepository.findShippingWithDeadlineToday();
		log.info("dailyDeadLines.size(): {}", dailyDeadLines.size());
		for(Shipping shipping : dailyDeadLines) {
			sendSlackNotification(shipping.getOrderId());
		}
	}

	private void sendSlackNotification(UUID orderId) {
		messageClient.generateAndSendShippingTimeMessage(orderId);
	}
}
