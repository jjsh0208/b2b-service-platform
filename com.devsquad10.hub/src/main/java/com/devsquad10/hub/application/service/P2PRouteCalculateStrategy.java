package com.devsquad10.hub.application.service;

import java.util.Collections;

import org.springframework.stereotype.Service;

import com.devsquad10.hub.application.dto.RouteCalculationResult;
import com.devsquad10.hub.domain.model.Hub;
import com.devsquad10.hub.infrastructure.client.NaverDirections5Client;
import com.devsquad10.hub.infrastructure.client.dto.NaverDirections5Response;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class P2PRouteCalculateStrategy implements HubRouteCalculateStrategy {

	private final NaverDirections5Client naverClient;

	@Override
	public RouteCalculationResult calculateRoute(Hub departureHub, Hub destinationHub) {
		// 하버사인 공식을 이용한 거리 계산(미터 단위)
		double distance = calculateHaversineDistance(
			departureHub.getLatitude(), departureHub.getLongitude(),
			destinationHub.getLatitude(), destinationHub.getLongitude()
		);

		// 평균 50km/h로 이동 한다 가정
		final double SPEED_KMH = 50.0;

		// 이동 예상 시간 (밀리 초)
		int durationInMillis = (int)((distance * 0.001) / SPEED_KMH * (3600 * 1000));

		return RouteCalculationResult.builder()
			.distance(distance)
			.duration(durationInMillis)
			.waypoint(Collections.emptyList())
			.build();
	}

	@Override
	public RouteCalculationResult calculateRouteWithApi(Hub departureHub, Hub destinationHub) {
		NaverDirections5Response apiResult = naverClient.getDistanceAndDuration(
			departureHub.getLatitude(), departureHub.getLongitude(),
			destinationHub.getLatitude(), destinationHub.getLongitude()
		);

		return RouteCalculationResult.builder()
			.distance(Double.valueOf(apiResult.getDistance()))
			.duration(apiResult.getDuration())
			.waypoint(Collections.emptyList())
			.build();
	}

	/**
	 * 하버사인 공식을 사용한 두 지점 간 거리 계산
	 * @param lat1 위도1
	 * @param lon1 경도1
	 * @param lat2 위도2
	 * @param lon2 경도2
	 * @return 두 좌표의 거리 (미터 단위)
	 */
	private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
		final double R = 6378.137; // 지구 반지름 길이(km)

		double dLat = Math.toRadians(lat2 - lat1); // 위도 차
		double dLon = Math.toRadians(lon2 - lon1); // 경도 차

		double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(Math.toRadians(lat1))
			* Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLon / 2), 2);

		// 원래 공식인 2·r·arcsin(√a) 대신 2·atan2(√a, √(1–a))가 오차 전파가 적어 안정적임
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return R * c * 1000;
	}
}
