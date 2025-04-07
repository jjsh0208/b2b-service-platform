package com.devsquad10.shipping.infrastructure.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.devsquad10.shipping.infrastructure.client.dto.HubFeignClientGetRequest;

@FeignClient(name = "hubRoute", url = "http://hub:8080/api/hub-route")
public interface HubRouteClient {
	// 출발허브 ID와 도착허브 ID와 일치하는
	// 허브간 경유지 & 각 허브구간별 예상시간과 예상거리 응답 받는 API 호출
	@GetMapping("/info/{departureHubId}/{destinationHubId}")
	List<HubFeignClientGetRequest> getHubRouteInfo (
		@PathVariable("departureHubId") UUID departureHubId,
		@PathVariable("destinationHubId") UUID destinationHubId
	);
}
