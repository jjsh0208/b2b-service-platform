package com.devsquad10.message.application.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.devsquad10.message.application.dto.req.GeminiRequestDto;
import com.devsquad10.message.infrastructure.client.dto.ShippingClientDataRequestDto;
import com.devsquad10.message.infrastructure.client.exception.RestClientApiCallException;
import com.devsquad10.message.infrastructure.client.exception.RestClientApiParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiService {

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	@Value("${gemini.api.key}")
	private String apiKey;

	@Value("${gemini.api.url}")
	private String apiUrl;

	public String generateShippingTimeMessage(ShippingClientDataRequestDto request) {
		// AI 서비스에 필요한 정보를 전달하고 응답 생성
		String prompt = buildPrompt(request);

		return generateText(prompt);
	}

	public String generateText(String prompt) {
		URI url = UriComponentsBuilder.fromUriString(apiUrl)
			.queryParam("key", apiKey)
			.build()
			.toUri();

		GeminiRequestDto requestBody = GeminiRequestDto.from(prompt);

		try {
			// API 호출 및 응답 받기
			String responseJson = restClient.post()
				.uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.body(requestBody)
				.retrieve()
				.body(String.class);

			// 응답에서 text 값만 추출
			return extractTextFromResponse(responseJson);
		} catch (Exception e) {
			throw new RestClientApiCallException("Gemini API 호출 실패: " + e.getMessage());
		}
	}

	/**
	 * Gemini API 응답에서 text 값만 추출
	 */
	private String extractTextFromResponse(String responseJson) {
		try {
			JsonNode rootNode = objectMapper.readTree(responseJson);

			// candidates[0].content.parts[0].text 경로로 값 추출
			return rootNode
				.path("candidates")
				.path(0)
				.path("content")
				.path("parts")
				.path(0)
				.path("text")
				.asText();
		} catch (Exception e) {
			throw new RestClientApiParseException("응답 파싱 실패: " + e.getMessage());
		}
	}

	private String buildPrompt(ShippingClientDataRequestDto request) {
		return String.format("""
				- 주문 번호: %s
				- 주문자 정보: %s
				- 상품 정보: %s %s박스
				- 요청 사항: %s
				- 발송지: %s
				- 경유지: %s
				- 도착지: %s
				- 배송담당자: %s
				
				- 위 내용을 기반으로 도출된 최종 발송 시한은 [ ] 시 입니다.
				
				해당 정보의 양식으로만 출력하고, 대략적인 최종 발송 시한을 알려주세요.
				""",
			request.getOrderId(),
			request.getCustomerName(),
			request.getProductInfo(),
			request.getQuantity(),
			request.getRequestDetails(),
			request.getDepartureHubName(),
			request.getWaypointHubNames(),
			request.getAddress(),
			request.getShippingManagerName()
		);
	}

}
