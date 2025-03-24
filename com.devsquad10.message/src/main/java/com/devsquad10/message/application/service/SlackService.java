package com.devsquad10.message.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.devsquad10.message.application.dto.req.SlackIncomingHookDto;
import com.devsquad10.message.application.dto.req.SlackMessageRequestDto;
import com.devsquad10.message.application.dto.res.MessageResponseDto;
import com.devsquad10.message.application.exception.SlackApiException;
import com.devsquad10.message.application.exception.SlackUserNotFoundException;
import com.devsquad10.message.domain.model.Message;
import com.devsquad10.message.domain.repository.MessageRepository;
import com.devsquad10.message.infrastructure.client.ShippingClient;
import com.devsquad10.message.infrastructure.client.dto.ShippingClientDataRequestDto;
import com.devsquad10.message.infrastructure.client.dto.ShippingClientDataResponseDto;
import com.devsquad10.message.infrastructure.client.dto.SoldOutMessageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlackService {
	private final MessageRepository messageRepository;
	private final ObjectMapper objectMapper;
	private final RestClient restClient;
	private final AiService aiService;
	private final ShippingClient shippingClient;

	@Value("${slack.api.key}")
	private String slackOAuthToken;

	@Value("${slack.api.url}")
	private String slackUrl;

	@Transactional
	public MessageResponseDto sendMessage(SlackMessageRequestDto requestDto) {
		Message message = Message.builder()
			.message(requestDto.getMessage())
			.recipientId(requestDto.getReceiverId())
			.build();

		Message savedMessage = messageRepository.save(message);

		// 사용자 ID를 얻어오기 위해 username을 통해 ID를 찾음
		String userId = getSlackUserIdByUsername(requestDto.getReceiverId());

		if (userId != null) {
			String messageText = "<@" + userId + "> " + requestDto.getMessage();

			// SlackIncomingHookDto 객체 생성 후 메시지 전송
			SlackIncomingHookDto hookRequest = SlackIncomingHookDto.builder()
				.channel(requestDto.getChannel())
				.text(messageText)
				.build();

			restClient.post()
				.uri(slackUrl)
				.contentType(MediaType.APPLICATION_JSON)
				.body(hookRequest)
				.retrieve()
				.toBodilessEntity();

		} else {
			throw new SlackUserNotFoundException("슬랙 사용자를 찾을 수 없습니다: " + requestDto.getReceiverId());
		}

		return MessageResponseDto.fromEntity(savedMessage);
	}

	/**
	 * Slack 사용자 목록에서 username에 해당하는 사용자의 Slack ID를 찾는 메서드
	 * @param username Slack 사용자 이름
	 * @return 사용자 ID
	 */
	public String getSlackUserIdByUsername(String username) {
		String url = "https://slack.com/api/users.list";

		// Slack API 호출
		String responseBody = restClient.get()
			.uri(url)
			.header("Authorization", "Bearer " + slackOAuthToken)
			.retrieve()
			.body(String.class);

		if (responseBody != null) {
			List<String> userIds = parseUserList(responseBody, username);

			if (!userIds.isEmpty()) {
				return userIds.get(0);  // 첫 번째 일치하는 사용자의 ID를 반환
			}
		}

		// 사용자를 찾지 못하면 null 반환
		return null;
	}

	/**
	 * JSON 응답에서 사용자 목록을 파싱하여 username과 일치하는 사용자 ID를 찾는 메서드
	 * @param jsonResponse Slack API 응답 JSON
	 * @param username Slack 사용자 이름
	 * @return 사용자 ID 목록
	 */
	private List<String> parseUserList(String jsonResponse, String username) {
		List<String> userIds = new ArrayList<>();

		try {
			//  Jackson ObjectMapper를 사용하여 JSON 응답 파싱
			JsonNode rootNode = objectMapper.readTree(jsonResponse);
			JsonNode membersNode = rootNode.path("members");

			// 사용자 목록 순회하여 일치하는 사용자 ID를 찾음
			for (JsonNode memberNode : membersNode) {
				String realName = memberNode.path("real_name").asText();
				String userId = memberNode.path("id").asText();

				// 사용자 이름이 일치하는 경우 해당 사용자 ID를 목록에 추가
				if (realName.equalsIgnoreCase(username)) {
					userIds.add(userId);
				}

				// 만약 real_name이 아닌 profile.display_name을 사용하려면:
				// String displayName = memberNode.path("profile").path("display_name").asText();
				// if (displayName.equalsIgnoreCase(username)) {
				//     userIds.add(userId);
				// }
			}
		} catch (Exception e) {
			throw new SlackApiException("사용자 목록 파싱 실패: " + e.getMessage());
		}

		return userIds;
	}

	// TODO : 동작 검증 필요
	@Transactional
	public ShippingClientDataResponseDto sendShippingTimeNotification(UUID orderId) {

		// 배송 정보 조회
		ShippingClientDataRequestDto shippingData = shippingClient.getShippingClientData(orderId);

		if (shippingData == null) {
			throw new EntityNotFoundException("주문 ID에 해당하는 배송 정보를 찾을 수 없습니다: " + orderId);
		}

		// AiService를 통한 메시지 생성
		String generatedMessage = aiService.generateShippingTimeMessage(shippingData);

		// 메시지 저장
		Message message = Message.builder()
			.message(generatedMessage)
			.recipientId(shippingData.getShippingManagerName())
			.build();

		Message savedMessage = messageRepository.save(message);

		// Slack 메시지 전송
		SlackMessageRequestDto requestDto = SlackMessageRequestDto.builder()
			.receiverId(shippingData.getShippingManagerName())
			.message(generatedMessage)
			.channel("#message")
			.build();

		sendMessage(requestDto);

		return ShippingClientDataResponseDto.fromEntity(savedMessage);
	}

	@Transactional
	public void sendSoldOutNotification(SoldOutMessageRequest request) {
		String formattedMessage = String.format(
			"\n🔹 상품명: %s\n📅 재고 소진 일자: %s\n⚠ 알림: 해당 상품의 재고가 모두 소진되었습니다. 빠른 조치 바랍니다.",
			request.getProductName(),
			request.getSoldOutAt()
		);

		Message message = Message.builder()
			.message(formattedMessage)
			.recipientId(request.getVenderSlackId())
			.build();

		messageRepository.save(message);

		SlackMessageRequestDto slackRequest = SlackMessageRequestDto.builder()
			.receiverId(request.getVenderSlackId())
			.message(formattedMessage)
			.channel("#message")
			.build();

		sendMessage(slackRequest);
	}
}
