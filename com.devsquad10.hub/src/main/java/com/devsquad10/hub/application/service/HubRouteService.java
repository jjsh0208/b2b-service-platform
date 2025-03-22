package com.devsquad10.hub.application.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.hub.application.dto.RouteCalculationResult;
import com.devsquad10.hub.application.dto.enums.HubRouteStrategyType;
import com.devsquad10.hub.application.dto.req.HubRouteCreateRequestDto;
import com.devsquad10.hub.application.dto.req.HubRouteSearchRequestDto;
import com.devsquad10.hub.application.dto.req.HubRouteUpdateRequestDto;
import com.devsquad10.hub.application.dto.res.HubRouteCreateResponseDto;
import com.devsquad10.hub.application.dto.res.HubRouteGetOneResponseDto;
import com.devsquad10.hub.application.dto.res.HubRouteUpdateResponseDto;
import com.devsquad10.hub.application.dto.res.HubRouteWaypointDto;
import com.devsquad10.hub.application.dto.res.PagedHubRouteItemResponseDto;
import com.devsquad10.hub.application.dto.res.PagedHubRouteResponseDto;
import com.devsquad10.hub.application.exception.HubNotFoundException;
import com.devsquad10.hub.application.exception.HubRouteNotFoundException;
import com.devsquad10.hub.application.exception.RouteStrategySelectionException;
import com.devsquad10.hub.domain.model.Hub;
import com.devsquad10.hub.domain.model.HubRoute;
import com.devsquad10.hub.domain.model.HubRouteWaypoint;
import com.devsquad10.hub.domain.repository.HubRepository;
import com.devsquad10.hub.domain.repository.HubRouteRepository;
import com.devsquad10.hub.infrastructure.client.dto.HubFeignClientGetRequest;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class HubRouteService {

	private final HubRepository hubRepository;
	private final HubRouteRepository hubRouteRepository;
	private final P2PRouteCalculateStrategy p2pStrategy;
	private final HubToHubRelayCalculateStrategy hubToHubRelayStrategy;

	@Caching(evict = {
		@CacheEvict(value = "hubRouteSearchCache", allEntries = true)
	})
	public HubRouteCreateResponseDto createHubRoute(HubRouteCreateRequestDto request) {
		Hub departureHub = hubRepository.findById(request.getDepartureHubId())
			.orElseThrow(() -> new HubNotFoundException("출발 허브를 찾을 수 없습니다. ID: " + request.getDepartureHubId()));

		Hub destinationHub = hubRepository.findById(request.getDestinationHubId())
			.orElseThrow(() -> new HubNotFoundException("도착 허브를 찾을 수 없습니다. ID: " + request.getDestinationHubId()));

		Optional<HubRoute> existingRoute = hubRouteRepository.findByDepartureHubAndDestinationHub(
			departureHub, destinationHub);

		if (existingRoute.isPresent()) {
			return HubRouteCreateResponseDto.toResponseDto(existingRoute.get());
		}

		RouteCalculationResult calculationResult =
			executeSelectedStrategy(departureHub, destinationHub, request.getStrategyType());

		HubRoute newHubRoute = HubRoute.builder()
			.departureHub(departureHub)
			.destinationHub(destinationHub)
			.distance(calculationResult.getDistance())
			.duration(calculationResult.getDuration())
			.build();

		if (calculationResult.getWaypoint() != null) {
			// 경유지 ID 가져오기
			Set<UUID> waypointHubIds = new HashSet<>();

			for (HubRouteWaypointDto dto : calculationResult.getWaypoint()) {
				waypointHubIds.add(dto.getDepartureHubId());
				waypointHubIds.add(dto.getDestinationHubId());
			}

			Map<UUID, Hub> waypointHubMap = new HashMap<>();

			// 모든 경유 허브 조회
			for (Hub hub : hubRepository.findAllById(waypointHubIds)) {
				waypointHubMap.put(hub.getId(), hub);
			}

			List<HubRouteWaypoint> waypointEntities = calculationResult.getWaypoint().stream()
				.map(dto -> HubRouteWaypoint.builder()
					.hubRoute(newHubRoute)
					.departureHub(waypointHubMap.get(dto.getDepartureHubId()))
					.destinationHub(waypointHubMap.get(dto.getDestinationHubId()))
					.sequence(dto.getSequence())
					.distance(dto.getDistance())
					.duration(dto.getDuration())
					.build()
				)
				.toList();

			newHubRoute.getWaypoints().addAll(waypointEntities);
		}

		HubRoute savedRoute = hubRouteRepository.save(newHubRoute);

		return HubRouteCreateResponseDto.toResponseDto(savedRoute);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "hubRouteCache", key = "#id.toString()")
	public HubRouteGetOneResponseDto getOneHubRoute(UUID id) {
		HubRoute hubRoute = hubRouteRepository.findByIdWithWaypoints(id)
			.orElseThrow(() -> new HubRouteNotFoundException("허브 경로를 찾을 수 없습니다."));

		return HubRouteGetOneResponseDto.toResponseDto(hubRoute);
	}

	@Caching(
		put = {@CachePut(value = "hubRouteCache", key = "#id.toString()")},
		evict = {@CacheEvict(value = "hubRouteSearchCache", allEntries = true)}
	)
	public HubRouteUpdateResponseDto updateHubRoute(UUID id, HubRouteUpdateRequestDto request) {
		HubRoute hubRoute = hubRouteRepository.findByIdWithWaypoints(id)
			.orElseThrow(() -> new HubRouteNotFoundException("허브 경로를 찾을 수 없습니다."));

		hubRoute.update(
			request.getDistance(),
			request.getDuration()
		);

		HubRoute updatedRoute = hubRouteRepository.save(hubRoute);

		return HubRouteUpdateResponseDto.toResponseDto(updatedRoute);
	}

	@Caching(evict = {
		@CacheEvict(value = "hubRouteCache", key = "#id.toString()"),
		@CacheEvict(value = "hubRouteSearchCache", allEntries = true)
	})
	public void deleteHubRoute(UUID id) {
		HubRoute hubRoute = hubRouteRepository.findById(id)
			.orElseThrow(() -> new HubRouteNotFoundException("허브 경로를 찾을 수 없습니다."));

		// TODO: 사용자 정보 구현 시 수정
		hubRoute.delete(UUID.randomUUID());
		hubRouteRepository.save(hubRoute);
	}

	@Cacheable(value = "hubRouteSearchCache",
		key = "#request.toString()"
	)
	public PagedHubRouteResponseDto getHub(HubRouteSearchRequestDto request) {
		Page<HubRoute> hubRoutePage = hubRouteRepository.findAll(request);

		Page<PagedHubRouteItemResponseDto> dtoPage = hubRoutePage.map(PagedHubRouteItemResponseDto::toResponseDto);

		return PagedHubRouteResponseDto.toResponseDto(dtoPage, request.getSortOption());
	}

	private RouteCalculationResult executeSelectedStrategy(Hub departureHub, Hub destinationHub,
		HubRouteStrategyType strategyType) {

		if (strategyType == null) {
			throw new RouteStrategySelectionException("Strategy type이 Null입니다.");
		}

		return switch (strategyType) {
			case P2P -> p2pStrategy.calculateRoute(departureHub, destinationHub);
			case P2P_API -> p2pStrategy.calculateRouteWithApi(departureHub, destinationHub);
			case HUB_TO_HUB_RELAY -> hubToHubRelayStrategy.calculateRoute(departureHub, destinationHub);

			default -> throw new RouteStrategySelectionException("유효하지 않은 전략 선택입니다.");
		};
	}

	// TODO: 중복 코드 리팩토링
	@Caching(evict = {
		@CacheEvict(value = "hubRouteSearchCache", allEntries = true)
	})
	public List<HubFeignClientGetRequest> getHubRouteInfo(UUID departureHubId, UUID destinationHubId) {
		Hub departureHub = hubRepository.findById(departureHubId)
			.orElseThrow(() -> new HubNotFoundException("출발 허브를 찾을 수 없습니다: " + departureHubId));

		Hub destinationHub = hubRepository.findById(destinationHubId)
			.orElseThrow(() -> new HubNotFoundException("도착 허브를 찾을 수 없습니다: " + destinationHubId));

		Optional<HubRoute> existingRoute = hubRouteRepository.findByDepartureHubAndDestinationHub(
			departureHub, destinationHub);

		if (existingRoute.isPresent()) {
			return HubFeignClientGetRequest.from(existingRoute.get());
		}

		RouteCalculationResult calculationResult = executeSelectedStrategy(departureHub, destinationHub,
			HubRouteStrategyType.HUB_TO_HUB_RELAY);

		HubRoute newHubRoute = HubRoute.builder()
			.departureHub(departureHub)
			.destinationHub(destinationHub)
			.distance(calculationResult.getDistance())
			.duration(calculationResult.getDuration())
			.build();

		if (calculationResult.getWaypoint() != null) {
			// 경유지 ID 가져오기
			Set<UUID> waypointHubIds = new HashSet<>();

			for (HubRouteWaypointDto dto : calculationResult.getWaypoint()) {
				waypointHubIds.add(dto.getDepartureHubId());
				waypointHubIds.add(dto.getDestinationHubId());
			}

			Map<UUID, Hub> waypointHubMap = new HashMap<>();

			// 모든 경유 허브 조회
			for (Hub hub : hubRepository.findAllById(waypointHubIds)) {
				waypointHubMap.put(hub.getId(), hub);
			}

			List<HubRouteWaypoint> waypointEntities = calculationResult.getWaypoint().stream()
				.map(dto -> HubRouteWaypoint.builder()
					.hubRoute(newHubRoute)
					.departureHub(waypointHubMap.get(dto.getDepartureHubId()))
					.destinationHub(waypointHubMap.get(dto.getDestinationHubId()))
					.sequence(dto.getSequence())
					.distance(dto.getDistance())
					.duration(dto.getDuration())
					.build()
				)
				.toList();

			newHubRoute.getWaypoints().addAll(waypointEntities);
		}

		HubRoute savedRoute = hubRouteRepository.save(newHubRoute);

		return HubFeignClientGetRequest.from(savedRoute);
	}
}
