package com.devsquad10.hub.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.devsquad10.hub.application.dto.RouteCalculationResult;
import com.devsquad10.hub.application.dto.res.HubRouteWaypointDto;
import com.devsquad10.hub.application.exception.RouteNotFoundException;
import com.devsquad10.hub.application.exception.RouteSameHubException;
import com.devsquad10.hub.domain.model.Hub;
import com.devsquad10.hub.domain.model.HubConnection;
import com.devsquad10.hub.domain.repository.HubConnectionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HubToHubRelayCalculateStrategy implements HubRouteCalculateStrategy {

	private final HubConnectionRepository hubConnectionRepository;

	@Override
	public RouteCalculationResult calculateRouteWithApi(Hub departureHub, Hub destinationHub) {
		return null;
	}

	// TODO: 알고리즘 효율 확인 필요
	@Override
	public RouteCalculationResult calculateRoute(Hub departureHub, Hub destinationHub) {

		// 평균 속도 50Km/h라 가정
		final double SPEED_KMH = 50.0;

		if (departureHub.getId().equals(destinationHub.getId())) {
			throw new RouteSameHubException("출발지와 도착지가 같아 경로를 생성할 수 없습니다.");
		}

		List<HubConnection> connections = hubConnectionRepository.findAllByActiveTrue();

		Map<UUID, Hub> hubMap = new HashMap<>();

		// 출발지와 도착지 추가
		hubMap.put(departureHub.getId(), departureHub);
		hubMap.put(destinationHub.getId(), destinationHub);

		// 모든 허브 추가
		for (HubConnection conn : connections) {
			hubMap.putIfAbsent(conn.getHub().getId(), conn.getHub());
			hubMap.putIfAbsent(conn.getConnectedHub().getId(), conn.getConnectedHub());
		}

		// 인접 리스트
		Map<UUID, List<HubConnection>> neighborList = new HashMap<>();
		for (HubConnection conn : connections) {
			UUID hubId = conn.getHub().getId();
			if (!neighborList.containsKey(hubId)) {
				neighborList.put(hubId, new ArrayList<>());
			}
			neighborList.get(hubId).add(conn);
		}

		// 허브 ID -> 출발지로부터의 최단 거리
		Map<UUID, Double> distances = new HashMap<>();
		// 허브 ID -> 이전 허브 ID (경로 추적용)
		Map<UUID, UUID> previousNodes = new HashMap<>();
		// 방문 여부
		Set<UUID> visited = new HashSet<>();

		// 모든 거리를 최대값으로 설정
		for (UUID hubId : hubMap.keySet()) {
			distances.put(hubId, Double.MAX_VALUE);
		}

		// 출발지 거리는 0
		distances.put(departureHub.getId(), 0.0);

		// 우선순위 큐 설정 (거리가 가장 짧은 노드부터 처리)
		PriorityQueue<UUID> pq = new PriorityQueue<>(
			Comparator.comparingDouble(distances::get)
		);
		pq.add(departureHub.getId());

		// 다익스트라 메인 루프
		while (!pq.isEmpty()) {
			UUID currentId = pq.poll();

			// 이미 방문한 노드는 스킵
			if (visited.contains(currentId))
				continue;
			visited.add(currentId);

			// 목적지에 도달하면 종료
			if (currentId.equals(destinationHub.getId()))
				break;

			// 현재 노드에서 인접한 모든 노드 확인
			List<HubConnection> neighbors = neighborList.getOrDefault(currentId, Collections.emptyList());

			for (HubConnection conn : neighbors) {
				UUID neighborId = conn.getConnectedHub().getId();

				// 이미 방문한 노드는 스킵
				if (visited.contains(neighborId))
					continue;

				// 현재 노드를 통해 인접 노드로 가는 새로운 거리 계산
				double weight = conn.getWeight(); // 가중치 (이동 시간)
				double newDistance = distances.get(currentId) + weight;

				// 더 짧은 경로를 찾은 경우 업데이트
				if (newDistance < distances.get(neighborId)) {
					distances.put(neighborId, newDistance);
					previousNodes.put(neighborId, currentId);

					// 더 짧은 경우 pq에 추가
					pq.add(neighborId);
				}
			}
		}

		// 목적지까지 경로가 없는 경우
		if (distances.get(destinationHub.getId()) == Double.MAX_VALUE) {
			throw new RouteNotFoundException("출발지에서 목적지까지의 경로를 찾을 수 없습니다.");
		}

		// 경로 재구성 (도착지에서 출발지까지 역순)
		List<UUID> pathIds = new ArrayList<>();
		UUID current = destinationHub.getId();

		while (current != null) {
			pathIds.add(0, current); // 리스트 맨 앞에 추가
			current = previousNodes.get(current);
		}

		// 경로에 따른 허브 리스트 생성
		List<Hub> hubPath = new ArrayList<>();
		List<HubRouteWaypointDto> waypoints = new ArrayList<>();

		double totalDistance = 0.0;
		int totalDuration = 0;

		// 각 구간별 거리와 시간 계산
		for (int i = 0; i < pathIds.size() - 1; i++) {
			UUID currentId = pathIds.get(i);
			UUID nextId = pathIds.get(i + 1);

			Hub currentHub = hubMap.get(currentId);
			Hub nextHub = hubMap.get(nextId);

			// 경로 내 허브 추가
			if (i == 0)
				hubPath.add(currentHub);
			hubPath.add(nextHub);

			// 현재 구간의 연결 정보 찾기
			HubConnection connection = connections.stream()
				.filter(conn -> conn.getHub().getId().equals(currentId) &&
					conn.getConnectedHub().getId().equals(nextId))
				.findFirst()
				.orElseThrow(() -> new RouteNotFoundException("연결 정보를 찾을 수 없습니다"));

			// 시간(가중치)
			int duration = connection.getWeight();

			// overflow 방지
			// 거리 계산 (시간(초) * 속도(m/s))
			BigDecimal distanceBD = BigDecimal.valueOf(duration)
				.multiply(BigDecimal.valueOf(SPEED_KMH / 3600.0));

			double distance = distanceBD.doubleValue();

			totalDistance += distance;
			totalDuration += duration;

			HubRouteWaypointDto waypointDto = HubRouteWaypointDto.builder()
				.departureHubId(currentHub.getId())
				.destinationHubId(nextHub.getId())
				.sequence(i + 1)
				.distance(distance)
				.duration(duration)
				.build();

			waypoints.add(waypointDto);
		}

		return RouteCalculationResult.builder()
			.distance(totalDistance)
			.duration(totalDuration)
			.waypoint(waypoints)
			.build();
	}
}
