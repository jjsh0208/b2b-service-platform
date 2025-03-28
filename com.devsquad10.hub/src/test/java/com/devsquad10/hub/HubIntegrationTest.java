package com.devsquad10.hub;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.devsquad10.hub.application.dto.enums.HubSortOption;
import com.devsquad10.hub.application.dto.req.HubCreateRequestDto;
import com.devsquad10.hub.application.dto.req.HubSearchRequestDto;
import com.devsquad10.hub.application.dto.req.HubUpdateRequestDto;
import com.devsquad10.hub.application.dto.res.HubCreateResponseDto;
import com.devsquad10.hub.application.dto.res.HubGetOneResponseDto;
import com.devsquad10.hub.application.dto.res.HubUpdateResponseDto;
import com.devsquad10.hub.application.dto.res.PagedHubResponseDto;
import com.devsquad10.hub.application.exception.HubNotFoundException;
import com.devsquad10.hub.application.service.HubService;
import com.devsquad10.hub.domain.model.Hub;
import com.devsquad10.hub.domain.repository.HubRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class HubIntegrationTest {

	@Autowired
	private HubRepository hubRepository;

	@Autowired
	private HubService hubService;

	@Test
	@DisplayName("허브 생성 정상요청")
	void testHubPostSuccess() {
		// given
		HubCreateRequestDto request = HubCreateRequestDto.builder()
			.name("테스트 허브")
			.address("테스트 주소")
			.latitude(37.5665)
			.longitude(126.9780)
			.build();

		// when
		HubCreateResponseDto response = hubService.createHub(request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getName()).isEqualTo(request.getName());
		assertThat(response.getId()).isNotNull();
	}

	@Test
	@DisplayName("허브 단일 조회 성공")
	void testGetOneHubSuccess() {
		// given
		HubCreateRequestDto createRequest = HubCreateRequestDto.builder()
			.name("조회 테스트 허브")
			.address("조회 테스트 주소")
			.latitude(35.1234)
			.longitude(129.0987)
			.build();

		HubCreateResponseDto createResponse = hubService.createHub(createRequest);
		UUID hubId = createResponse.getId();

		// when
		HubGetOneResponseDto response = hubService.getOneHub(hubId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(hubId);
		assertThat(response.getName()).isEqualTo(createRequest.getName());
	}

	@Test
	@DisplayName("존재하지 않는 허브 조회 시 예외 발생")
	void testGetOneHubNotFound() {
		// given
		UUID nonExistentId = UUID.fromString("00000000-0000-0000-0000-000000000000");

		// when & then
		assertThatThrownBy(() -> hubService.getOneHub(nonExistentId))
			.isInstanceOf(HubNotFoundException.class);
	}

	@Test
	@DisplayName("허브 정보 수정 성공")
	void testUpdateHubSuccess() {
		// given
		HubCreateRequestDto createRequest = HubCreateRequestDto.builder()
			.name("수정 전 허브")
			.address("수정 전 주소")
			.latitude(36.5678)
			.longitude(128.4321)
			.build();

		HubCreateResponseDto createResponse = hubService.createHub(createRequest);
		UUID hubId = createResponse.getId();

		HubUpdateRequestDto updateRequest = HubUpdateRequestDto.builder()
			.name("수정 후 허브")
			.address("수정 후 주소")
			.latitude(36.9999)
			.longitude(128.8888)
			.build();

		// when
		HubUpdateResponseDto updateResponse = hubService.updateHub(hubId, updateRequest);

		// then
		assertThat(updateResponse).isNotNull();
		assertThat(updateResponse.getId()).isEqualTo(hubId);
		assertThat(updateResponse.getName()).isEqualTo(updateRequest.getName());
		assertThat(updateResponse.getAddress()).isEqualTo(updateRequest.getAddress());
		assertThat(updateResponse.getLatitude()).isEqualTo(updateRequest.getLatitude());
		assertThat(updateResponse.getLongitude()).isEqualTo(updateRequest.getLongitude());
	}

	@Test
	@DisplayName("존재하지 않는 허브 수정 시 예외 발생")
	void testUpdateHubNotFound() {
		// given
		UUID nonExistentId = UUID.randomUUID();
		HubUpdateRequestDto updateRequest = HubUpdateRequestDto.builder()
			.name("수정 후 허브")
			.address("수정 후 주소")
			.latitude(36.9999)
			.longitude(128.8888)
			.build();

		// when & then
		assertThatThrownBy(() -> hubService.updateHub(nonExistentId, updateRequest))
			.isInstanceOf(HubNotFoundException.class);
	}

	@Test
	@DisplayName("허브 삭제 성공")
	void testDeleteHubSuccess() {
		// given
		HubCreateRequestDto createRequest = HubCreateRequestDto.builder()
			.name("삭제 테스트 허브")
			.address("삭제 테스트 주소")
			.latitude(33.3333)
			.longitude(127.7777)
			.build();

		HubCreateResponseDto createResponse = hubService.createHub(createRequest);
		UUID hubId = createResponse.getId();

		// when
		hubService.deleteHub(hubId);

		// then
		// 논리적 삭제이므로 엔티티는 존재함
		Optional<Hub> foundHub = hubRepository.findById(hubId);
		assertThat(foundHub).isPresent();
		assertThat(foundHub.get().getDeletedAt()).isNotNull();
	}

	@Test
	@DisplayName("존재하지 않는 허브 삭제 시 예외 발생")
	void testDeleteHubNotFound() {
		// given
		UUID nonExistentId = UUID.randomUUID();

		// when & then
		assertThatThrownBy(() -> hubService.deleteHub(nonExistentId))
			.isInstanceOf(HubNotFoundException.class)
			.hasMessageContaining("Hub not found");
	}

	@Test
	@DisplayName("허브 목록 조회 성공")
	void testGetHubListSuccess() {

		// 테스트 허브 여러 개 생성
		for (int i = 1; i <= 5; i++) {
			HubCreateRequestDto request = HubCreateRequestDto.builder()
				.name("테스트 허브 " + i)
				.address("테스트 주소 " + i)
				.latitude(37.5 + i * 0.1)
				.longitude(127.0 + i * 0.1)
				.build();

			hubService.createHub(request);
		}

		HubSearchRequestDto searchRequest = new HubSearchRequestDto();
		searchRequest.setPage(0);
		searchRequest.setSize(10);
		searchRequest.setSortOption(HubSortOption.CREATED_AT);
		searchRequest.setSortOrder(Sort.Direction.DESC);

		// when
		PagedHubResponseDto response = hubService.getHub(searchRequest);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getHubs()).isNotNull();
		assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(2);
	}

	@Test
	@DisplayName("허브 존재 여부 확인 - 존재하는 경우")
	void testHubExistsTrue() {
		// given
		HubCreateRequestDto request = HubCreateRequestDto.builder()
			.name("존재 확인 허브")
			.address("존재 확인 주소")
			.latitude(35.5555)
			.longitude(125.5555)
			.build();

		HubCreateResponseDto createResponse = hubService.createHub(request);
		UUID hubId = createResponse.getId();

		// when
		boolean exists = hubService.existById(hubId);

		// then
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("허브 존재 여부 확인 - 존재하지 않는 경우")
	void testHubExistsFalse() {
		// given
		UUID nonExistentId = UUID.randomUUID();

		// when
		boolean exists = hubService.existById(nonExistentId);

		// then
		assertThat(exists).isFalse();
	}
}
