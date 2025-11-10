package com.devsquad10.hub;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.devsquad10.hub.application.dto.enums.HubSortOption;
import com.devsquad10.hub.application.dto.req.HubSearchRequestDto;
import com.devsquad10.hub.domain.model.Hub;
import com.devsquad10.hub.domain.model.HubConnection;
import com.devsquad10.hub.domain.repository.HubConnectionRepository;
import com.devsquad10.hub.domain.repository.HubRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class HubInitDataTest {
	@Autowired
	private HubRepository hubRepository;

	@Autowired
	private HubConnectionRepository hubConnectionRepository;

	@Test
	@DisplayName("Hub 테이블에 17개의 기본 데이터가 로드되었는지 확인")
	void testHubDataLoaded() {
		// given
		HubSearchRequestDto searchRequest = HubSearchRequestDto.builder()
			.page(0)
			.size(20)
			.sortOption(HubSortOption.CREATED_AT)
			.sortOrder(Sort.Direction.ASC)
			.build();

		// when
		Page<Hub> hubPage = hubRepository.findAll(searchRequest);

		// then
		assertThat(hubPage.getTotalElements()).isEqualTo(17);

		// 1페이지 당 크기는 10,30,50 고정 (기본 10)
		assertThat(hubPage.getNumber()).isNotEqualTo(1);
		assertThat(hubPage.getTotalPages()).isEqualTo(2);
	}

	@Test
	@DisplayName("Hub Connection 테이블에 데이터가 정상적으로 들어갔는지 확인")
	void testHubConnectionDataLoaded() {
		List<HubConnection> connections = hubConnectionRepository.findAllByActiveTrue();

		assertThat(connections.size()).isGreaterThan(1);
	}
}
