package com.devsquad10.hub.infrastructure.client;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.devsquad10.hub.infrastructure.client.dto.NaverDirections5Response;
import com.devsquad10.hub.infrastructure.client.exception.RestClientApiCallException;

import lombok.RequiredArgsConstructor;

/**
 * NAVER Directions 5 API 참고
 * @link <a href="https://api.ncloud-docs.com/docs/ai-naver-mapsdirections-driving">
 * NAVER Directions 5 API
 * </a>
 */
@Component
@RequiredArgsConstructor
public class NaverDirections5Client {

	private final RestClient restClient;

	@Value("${naver.api.client-id}")
	private String clientId;

	@Value("${naver.api.client-secret}")
	private String clientSecret;

	@Value("${naver.api.url}")
	private String clientUrl;

	public NaverDirections5Response getDistanceAndDuration(
		Double latitude,
		Double longitude,
		Double destinationHubLatitude,
		Double destinationHubLongitude
	) {

		URI url = UriComponentsBuilder.fromUriString(clientUrl)
			.queryParam("start", longitude + "," + latitude)
			.queryParam("goal", destinationHubLongitude + "," + destinationHubLatitude)
			.build()
			.toUri();

		ResponseEntity<String> response = restClient.get()
			.uri(url)
			.headers(headers -> {
				headers.set("x-ncp-apigw-api-key-id", clientId);
				headers.set("x-ncp-apigw-api-key", clientSecret);
				headers.setContentType(MediaType.APPLICATION_JSON);
			})
			.retrieve()
			.toEntity(String.class);

		return parseResponse(response.getBody());
	}

	private NaverDirections5Response parseResponse(String responseBody) {
		try {
			JSONObject jsonResponse = new JSONObject(responseBody);
			JSONObject summary = jsonResponse.getJSONObject("route")
				.getJSONArray("traoptimal")
				.getJSONObject(0)
				.getJSONObject("summary");

			Integer distance = summary.getInt("distance");
			Integer duration = summary.getInt("duration");

			return NaverDirections5Response.builder()
				.distance(distance)
				.duration(duration)
				.build();
		} catch (Exception e) {
			throw new RestClientApiCallException("API 응답 파싱 실패: " + e.getMessage());
		}
	}
}
