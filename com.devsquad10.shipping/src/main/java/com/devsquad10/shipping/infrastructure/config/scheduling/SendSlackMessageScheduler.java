package com.devsquad10.shipping.infrastructure.config.scheduling;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.devsquad10.shipping.domain.model.Shipping;
import com.devsquad10.shipping.domain.repository.ShippingRepository;
import com.devsquad10.shipping.infrastructure.client.MessageClient;
import com.devsquad10.shipping.infrastructure.client.dto.ShippingClientDataRequestDto;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class SendSlackMessageScheduler {

	private final ShippingRepository shippingRepository;
	private final MessageClient messageClient;

	// @Scheduled(cron = "0 0 6 * * *") // 매일 오전 6시
	// @Scheduled(cron = "*/30 * * * * *") // 테스트용 30초 마다
	public void sendDailySlackNotification() {
		List<Shipping> dailyDeadLines = shippingRepository.findShippingWithDeadlineToday();
		log.info("dailyDeadLines.size(): {}", dailyDeadLines.size());

		if(!dailyDeadLines.isEmpty()) {
			for(Shipping shipping : dailyDeadLines) {
				log.info("CompanyShippingManagerId: {}", shipping.getCompanyShippingManagerId());
				ShippingClientDataRequestDto responseDto = sendSlackNotification(shipping.getOrderId());
				log.info("OrderId: {}", responseDto.getOrderId());
			}
		}
	}

	private ShippingClientDataRequestDto sendSlackNotification(UUID orderId) {
		ShippingClientDataRequestDto response = messageClient.getShippingClientData(orderId);
		if(response == null) {
			throw new EntityNotFoundException("슬랙 메시지가 내용이 없습니다.");
		}
		return response;
	}
}
