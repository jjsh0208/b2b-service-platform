package com.devsquad10.shipping.infrastructure.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hub", url = "http://localhost:19094/api/hub")
public interface HubClient {

	// 입력받은 hubId 가 존재하는 hub의 id 인지 유효성 검사하는 API 호출
	@GetMapping("/exists/{uuid}")
	Boolean isHubExists(@PathVariable(name = "uuid") UUID uuid);

	// 출발허브 ID와 도착허브 ID와 일치하는
	// 허브간 경유지 & 각 허브구간별 예상시간과 예상거리 응답 받는 API 호출
	@GetMapping("info/{departureHubId}/{destinationHubId}")
	List<HubFeignClientGetRequest> getHubRouteInfo (
		@PathVariable("departureHubId") UUID departureHubId,
		@PathVariable("destinationHubId") UUID destinationHubId
	);
}
