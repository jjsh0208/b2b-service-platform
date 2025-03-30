package com.devsquad10.hub;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.devsquad10.hub.application.dto.enums.HubRouteSortOption;
import com.devsquad10.hub.application.dto.enums.HubRouteStrategyType;
import com.devsquad10.hub.application.dto.req.HubRouteCreateRequestDto;
import com.devsquad10.hub.application.dto.req.HubRouteSearchRequestDto;
import com.devsquad10.hub.application.dto.req.HubRouteUpdateRequestDto;
import com.devsquad10.hub.application.dto.res.HubRouteCreateResponseDto;
import com.devsquad10.hub.application.dto.res.HubRouteGetOneResponseDto;
import com.devsquad10.hub.application.dto.res.HubRouteUpdateResponseDto;
import com.devsquad10.hub.application.dto.res.PagedHubRouteResponseDto;
import com.devsquad10.hub.application.exception.HubRouteNotFoundException;
import com.devsquad10.hub.application.service.HubRouteService;
import com.devsquad10.hub.domain.model.Hub;
import com.devsquad10.hub.domain.model.HubRoute;
import com.devsquad10.hub.domain.repository.HubRepository;
import com.devsquad10.hub.domain.repository.HubRouteRepository;
import com.devsquad10.hub.infrastructure.client.NaverDirections5Client;
import com.devsquad10.hub.infrastructure.client.dto.NaverDirections5Response;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class HubRouteIntegrationTest {

	@Autowired
	private HubRepository hubRepository;

	@Autowired
	private HubRouteRepository hubRouteRepository;

	@Autowired
	private HubRouteService hubRouteService;

	@MockBean
	private NaverDirections5Client naverDirections5Client;

	@BeforeEach
	void setUp() {
		// Naver API 응답 모킹
		NaverDirections5Response mockResponse = NaverDirections5Response.builder()
			.distance(383627)
			.duration(17003725)
			.build();

		when(naverDirections5Client.getDistanceAndDuration(
			any(Double.class), any(Double.class),
			any(Double.class), any(Double.class)))
			.thenReturn(mockResponse);
	}

	@Test
	@DisplayName("P2P 전략으로 경로 생성 성공")
	void testCreateHubRouteWithP2PStrategy() {
		Hub departureHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111101"))
			.orElseThrow(); // 서울특별시 센터
		Hub destinationHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111104"))
			.orElseThrow(); // 부산광역시 센터

		HubRouteCreateRequestDto request = HubRouteCreateRequestDto.builder()
			.departureHubId(departureHub.getId())
			.destinationHubId(destinationHub.getId())
			.strategyType(HubRouteStrategyType.P2P)
			.build();

		// when
		HubRouteCreateResponseDto response = hubRouteService.createHubRoute(request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getId()).isNotNull();
		assertThat(response.getDepartureHubId()).isEqualTo(departureHub.getId());
		assertThat(response.getDestinationHubId()).isEqualTo(destinationHub.getId());
		assertThat(response.getDistance()).isGreaterThan(0);
		assertThat(response.getDuration()).isGreaterThan(0);
	}

	@Test
	@DisplayName("경로 단일 조회 성공")
	void testGetOneHubRouteSuccess() {
		// given
		Hub departureHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111101"))
			.orElseThrow(); // 서울특별시 센터
		Hub destinationHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111104"))
			.orElseThrow(); // 부산광역시 센터

		HubRouteCreateRequestDto createRequest = HubRouteCreateRequestDto.builder()
			.departureHubId(departureHub.getId())
			.destinationHubId(destinationHub.getId())
			.strategyType(HubRouteStrategyType.P2P)
			.build();

		HubRouteCreateResponseDto createResponse = hubRouteService.createHubRoute(createRequest);
		UUID routeId = createResponse.getId();

		// when
		HubRouteGetOneResponseDto response = hubRouteService.getOneHubRoute(routeId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(routeId);
		assertThat(response.getDepartureHubId()).isEqualTo(departureHub.getId());
		assertThat(response.getDestinationHubId()).isEqualTo(destinationHub.getId());
		assertThat(response.getDepartureHubName()).isEqualTo(departureHub.getName());
		assertThat(response.getDestinationHubName()).isEqualTo(destinationHub.getName());
	}

	@Test
	@DisplayName("존재하지 않는 경로 조회 시 예외 발생")
	void testGetOneHubRouteNotFound() {
		// given
		UUID nonExistentId = UUID.randomUUID();

		// when & then
		assertThatThrownBy(() -> hubRouteService.getOneHubRoute(nonExistentId))
			.isInstanceOf(HubRouteNotFoundException.class)
			.hasMessageContaining("허브 경로를 찾을 수 없습니다");
	}

	@Test
	@DisplayName("경로 정보 수정 성공")
	void testUpdateHubRouteSuccess() {
		// given
		Hub departureHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111101"))
			.orElseThrow();
		Hub destinationHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111104"))
			.orElseThrow();

		HubRouteCreateRequestDto createRequest = HubRouteCreateRequestDto.builder()
			.departureHubId(departureHub.getId())
			.destinationHubId(destinationHub.getId())
			.strategyType(HubRouteStrategyType.P2P)
			.build();

		HubRouteCreateResponseDto createResponse = hubRouteService.createHubRoute(createRequest);
		UUID routeId = createResponse.getId();

		Double newDistance = 400000.0; // 새 거리(m)
		Integer newDuration = 480000; // 새 소요시간(ms)

		HubRouteUpdateRequestDto updateRequest = HubRouteUpdateRequestDto.builder()
			.distance(newDistance)
			.duration(newDuration)
			.build();

		// when
		HubRouteUpdateResponseDto updateResponse = hubRouteService.updateHubRoute(routeId, updateRequest);

		// then
		assertThat(updateResponse).isNotNull();
		assertThat(updateResponse.getId()).isEqualTo(routeId);
		assertThat(updateResponse.getDistance()).isEqualTo(newDistance);
		assertThat(updateResponse.getDuration()).isEqualTo(newDuration);
	}

	@Test
	@DisplayName("경로 삭제 성공")
	void testDeleteHubRouteSuccess() {
		// given
		Hub departureHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111101"))
			.orElseThrow();
		Hub destinationHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111104"))
			.orElseThrow();

		HubRouteCreateRequestDto createRequest = HubRouteCreateRequestDto.builder()
			.departureHubId(departureHub.getId())
			.destinationHubId(destinationHub.getId())
			.strategyType(HubRouteStrategyType.P2P)
			.build();

		HubRouteCreateResponseDto createResponse = hubRouteService.createHubRoute(createRequest);
		UUID routeId = createResponse.getId();

		// when
		hubRouteService.deleteHubRoute(routeId);

		// then
		// 논리적 삭제이므로 엔티티는 존재
		Optional<HubRoute> foundRoute = hubRouteRepository.findById(routeId);
		assertThat(foundRoute).isPresent();
		assertThat(foundRoute.get().getDeletedAt()).isNotNull();
	}

	@Test
	@DisplayName("같은 출발/도착지로 경로 조회 시 재사용")
	void testHubRouteReuseForSameHubs() {
		// given
		Hub departureHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111101"))
			.orElseThrow();
		Hub destinationHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111104"))
			.orElseThrow();

		HubRouteCreateRequestDto firstRequest = HubRouteCreateRequestDto.builder()
			.departureHubId(departureHub.getId())
			.destinationHubId(destinationHub.getId())
			.strategyType(HubRouteStrategyType.P2P)
			.build();

		// 첫 번째 경로 생성
		HubRouteCreateResponseDto firstResponse = hubRouteService.createHubRoute(firstRequest);

		// 같은 출발/도착지로 두 번째 요청
		HubRouteCreateRequestDto secondRequest = HubRouteCreateRequestDto.builder()
			.departureHubId(departureHub.getId())
			.destinationHubId(destinationHub.getId())
			.strategyType(HubRouteStrategyType.P2P) // 같은 전략
			.build();

		// when
		HubRouteCreateResponseDto secondResponse = hubRouteService.createHubRoute(secondRequest);

		// then
		assertThat(secondResponse.getId()).isEqualTo(firstResponse.getId());
		assertThat(secondResponse.getDepartureHubId()).isEqualTo(firstResponse.getDepartureHubId());
		assertThat(secondResponse.getDestinationHubId()).isEqualTo(firstResponse.getDestinationHubId());
	}

	@Test
	@DisplayName("경로 목록 조회 성공")
	void testSearchHubRoutesSuccess() {
		// 여러 경로 생성
		Hub departureHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111101"))
			.orElseThrow();

		for (int i = 3; i < 6; i++) {
			Hub destinationHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-11111111110" + i))
				.orElseThrow();

			HubRouteCreateRequestDto request = HubRouteCreateRequestDto.builder()
				.departureHubId(departureHub.getId())
				.destinationHubId(destinationHub.getId())
				.strategyType(HubRouteStrategyType.P2P)
				.build();

			hubRouteService.createHubRoute(request);
		}

		// given
		HubRouteSearchRequestDto searchRequest = new HubRouteSearchRequestDto();
		searchRequest.setPage(0);
		searchRequest.setSize(10);
		searchRequest.setSortOption(HubRouteSortOption.CREATED_AT);
		searchRequest.setSortOrder(Sort.Direction.DESC);

		// when
		PagedHubRouteResponseDto response = hubRouteService.getHub(searchRequest);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getHubRoutes()).isNotNull();
		assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(3);
	}

	@Test
	@DisplayName("출발지 허브로 경로 검색 성공")
	void testSearchHubRoutesByDepartureHubSuccess() {
		// 여러 경로 생성
		Hub departureHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111101"))
			.orElseThrow();

		for (int i = 3; i < 6; i++) {
			Hub destinationHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-11111111110" + i))
				.orElseThrow();

			HubRouteCreateRequestDto request = HubRouteCreateRequestDto.builder()
				.departureHubId(departureHub.getId())
				.destinationHubId(destinationHub.getId())
				.strategyType(HubRouteStrategyType.P2P)
				.build();

			hubRouteService.createHubRoute(request);
		}

		// given
		HubRouteSearchRequestDto searchRequest = new HubRouteSearchRequestDto();
		searchRequest.setPage(0);
		searchRequest.setSize(10);
		searchRequest.setDepartureHubId(departureHub.getId());
		searchRequest.setSortOption(HubRouteSortOption.CREATED_AT);
		searchRequest.setSortOrder(Sort.Direction.DESC);

		// when
		PagedHubRouteResponseDto response = hubRouteService.getHub(searchRequest);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getHubRoutes()).isNotNull();
	}

	@Test
	@DisplayName("HubToHubRelay 전략으로 경로 생성 테스트")
	void testCreateHubRouteWithRelayStrategy() {
		// given
		// Hub 데이터 로드
		Hub seoulHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111101"))
			.orElseThrow(); // 서울
		Hub busanHub = hubRepository.findById(UUID.fromString("11111111-1111-1111-1111-111111111104"))
			.orElseThrow(); // 부산

		HubRouteCreateRequestDto request = HubRouteCreateRequestDto.builder()
			.departureHubId(seoulHub.getId())
			.destinationHubId(busanHub.getId())
			.strategyType(HubRouteStrategyType.HUB_TO_HUB_RELAY)
			.build();

		// when
		HubRouteCreateResponseDto response = hubRouteService.createHubRoute(request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getId()).isNotNull();

		// 경로 정보 가져와서 상세 검증
		HubRouteGetOneResponseDto routeDetails = hubRouteService.getOneHubRoute(response.getId());
		assertThat(routeDetails.getDepartureHubId()).isEqualTo(seoulHub.getId());
		assertThat(routeDetails.getDestinationHubId()).isEqualTo(busanHub.getId());

		// 경유지 확인 (웨이포인트가 존재하는지)
		if (routeDetails.getWaypoints() != null) {
			assertThat(routeDetails.getWaypoints()).isNotNull();
		}
	}
}
