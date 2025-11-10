// package com.devsquad10.shipping;
//
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;
//
// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.test.context.ActiveProfiles;
//
// import com.devsquad10.shipping.application.dto.request.ShippingAgentSearchReqDto;
// import com.devsquad10.shipping.application.dto.response.PagedShippingAgentResDto;
// import com.devsquad10.shipping.application.dto.response.ShippingAgentResDto;
// import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentNotFoundException;
// import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentNotUpdateException;
// import com.devsquad10.shipping.application.service.ShippingAgentService;
// import com.devsquad10.shipping.domain.enums.ShippingAgentType;
// import com.devsquad10.shipping.domain.model.ShippingAgent;
// import com.devsquad10.shipping.domain.repository.ShippingAgentRepository;
// import com.devsquad10.shipping.infrastructure.client.HubClient;
// import com.devsquad10.shipping.infrastructure.client.dto.ShippingAgentFeignClientPatchRequest;
// import com.devsquad10.shipping.infrastructure.client.dto.ShippingAgentFeignClientPostRequest;
//
// import jakarta.transaction.Transactional;
//
// @ExtendWith(MockitoExtension.class)
// @ActiveProfiles("test")
// @Transactional
// @SpringBootTest
// public class ShippingAgentServiceIntegrationTest {
//
// 	@Mock
// 	private HubClient hubClient;
//
// 	@Mock
// 	private ShippingAgentRepository shippingAgentRepository;
//
// 	@InjectMocks
// 	private ShippingAgentService shippingAgentService;
//
// 	private ShippingAgentFeignClientPostRequest postRequest;
// 	private ShippingAgentFeignClientPatchRequest patchRequest;
// 	private ShippingAgent shippingAgent;
// 	private UUID shippingManagerId;
// 	private UUID hubId;
// 	private UUID userId;
//
// 	@BeforeEach
// 	void setUp() {
// 		shippingManagerId = UUID.fromString("22fea2ae-eb76-43c8-839f-6b31d4c2dc41");
// 		hubId = UUID.fromString("11111111-1111-1111-1111-111111111107");
// 		userId = UUID.randomUUID();
//
// 		postRequest = ShippingAgentFeignClientPostRequest.builder()
// 			.shippingManagerId(shippingManagerId)
// 			.hubId(hubId)
// 			.slackId("slack123")
// 			.type(ShippingAgentType.COM_DVL)
// 			.build();
//
// 		patchRequest = ShippingAgentFeignClientPatchRequest.builder()
// 			.shippingManagerId(shippingManagerId)
// 			.hubId(hubId)
// 			.slackId("slack456")
// 			.build();
//
// 		shippingAgent = ShippingAgent.builder()
// 			.shippingManagerId(shippingManagerId)
// 			.hubId(hubId)
// 			.shippingManagerSlackId("slack123")
// 			.type(ShippingAgentType.COM_DVL)
// 			.shippingSequence(1)
// 			.isTransit(false)
// 			.assignmentCount(0)
// 			.build();
//
// 		shippingAgentRepository.save(shippingAgent);
// 	}
//
// 	@Test
// 	@DisplayName("배송담당자 등록 - 성공")
// 	void createShippingAgent_Success() {
// 		when(shippingAgentRepository.findMaxShippingSequence()).thenReturn(Optional.of(1));
// 		when(shippingAgentRepository.save(any(ShippingAgent.class))).thenReturn(shippingAgent);
// 		ShippingAgent savedShippingAgent = shippingAgentRepository.save(ShippingAgent.builder()
// 			.shippingManagerId(postRequest.getShippingManagerId())
// 			.hubId(postRequest.getHubId())
// 			.shippingManagerSlackId(postRequest.getSlackId())
// 			.type(postRequest.getType())
// 			.build());
//
// 		assertNotNull(shippingAgent.getId());
// 		assertEquals(ShippingAgentType.COM_DVL, savedShippingAgent.getType());
// 		verify(shippingAgentRepository, times(1)).save(any(ShippingAgent.class));
// 	}
//
// 	@Test
// 	@DisplayName("배송담당자 등록 - 실패")
// 	void createShippingAgent_HubNotFound() {
// 		when(hubClient.isHubExists(hubId)).thenReturn(false);
//
// 		boolean result = shippingAgentService.createShippingAgent(postRequest);
//
// 		assertFalse(result);
// 		verify(shippingAgentRepository, never()).save(any(ShippingAgent.class));
// 	}
//
// 	@Test
// 	@DisplayName("배송담당자 단일 조회 - 성공")
// 	void getShippingAgentById_Success() {
// 		when(shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId))
// 			.thenReturn(Optional.of(shippingAgent));
//
// 		ShippingAgentResDto result = shippingAgentService.getShippingAgentById(shippingManagerId);
//
// 		assertNotNull(result);
// 		assertEquals(shippingManagerId, result.getShippingManagerId());
// 	}
//
// 	@Test
// 	@DisplayName("배송담당자 단일 조회 - 실패")
// 	void getShippingAgentById_NotFound() {
// 		when(shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId))
// 			.thenReturn(Optional.empty());
//
// 		assertThrows(ShippingAgentNotFoundException.class, () -> shippingAgentService.getShippingAgentById(shippingManagerId));
// 	}
//
// 	@Test
// 	@DisplayName("배송담당자 검색 - 성공")
// 	void searchShippingAgents_Success() {
// 		ShippingAgentSearchReqDto searchReqDto = ShippingAgentSearchReqDto.builder()
// 			.page(0)
// 			.size(10)
// 			.build();
//
// 		Pageable pageable = PageRequest.of(0, 10);
// 		Page<ShippingAgent> shippingAgentPage = new PageImpl<>(List.of(shippingAgent), pageable, 1);
//
// 		when(shippingAgentRepository.findAll(searchReqDto)).thenReturn(shippingAgentPage);
//
// 		PagedShippingAgentResDto result = shippingAgentService.searchShippingAgents(searchReqDto);
//
// 		assertNotNull(result);
// 		assertEquals(10, result.getShippingAgents().size());
// 	}
//
// 	@Test
// 	@DisplayName("배송담당자 정보 수정 - 성공")
// 	void infoUpdateShippingAgent_Success() {
// 		when(shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId))
// 			.thenReturn(Optional.of(shippingAgent));
// 		when(hubClient.isHubExists(hubId)).thenReturn(true);
// 		when(shippingAgentRepository.save(any(ShippingAgent.class))).thenReturn(shippingAgent);
//
// 		boolean result = shippingAgentService.infoUpdateShippingAgent(patchRequest);
//
// 		assertTrue(result);
// 		verify(shippingAgentRepository, times(1)).save(any(ShippingAgent.class));
// 	}
//
// 	@Test
// 	@DisplayName("배송담당자 정보 수정 - 실패")
// 	void infoUpdateShippingAgent_NotFound() {
// 		when(shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId))
// 			.thenReturn(Optional.empty());
//
// 		assertThrows(ShippingAgentNotFoundException.class, () -> shippingAgentService.infoUpdateShippingAgent(patchRequest));
// 	}
//
// 	@Test
// 	@DisplayName("배송담당자 배송가능 여부 수정 - 성공")
// 	void transitUpdateShippingAgent_Success() {
// 		when(shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId))
// 			.thenReturn(Optional.of(shippingAgent));
// 		when(shippingAgentRepository.save(any(ShippingAgent.class))).thenReturn(shippingAgent);
//
// 		ShippingAgentResDto result = shippingAgentService.transitUpdateShippingAgent(shippingManagerId, false, userId);
//
// 		assertNotNull(result);
// 		assertEquals(true, result.getIsTransit());
// 		verify(shippingAgentRepository, times(1)).save(any(ShippingAgent.class));
// 	}
//
// 	@Test
// 	@DisplayName("배송담당자 배송가능 여부 수정 - 실패")
// 	void transitUpdateShippingAgent_SameTransit() {
// 		when(shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId))
// 			.thenReturn(Optional.of(shippingAgent));
//
// 		assertThrows(ShippingAgentNotUpdateException.class, () -> shippingAgentService.transitUpdateShippingAgent(shippingManagerId, false, userId));
// 	}
//
// 	@Test
// 	@DisplayName("배송담당자 삭제 - 성공")
// 	void deleteShippingAgentForUser_Success() {
// 		when(shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId))
// 			.thenReturn(Optional.of(shippingAgent));
// 		when(shippingAgentRepository.save(any(ShippingAgent.class))).thenReturn(shippingAgent);
//
// 		boolean result = shippingAgentService.deleteShippingAgentForUser(shippingManagerId);
//
// 		assertTrue(result);
// 		verify(shippingAgentRepository, times(1)).save(any(ShippingAgent.class));
// 	}
//
// 	@Test
// 	@DisplayName("배송담당자 삭제 - 실패")
// 	void deleteShippingAgentForUser_IsTransitTrue() {
// 		shippingAgent.updateIsTransit();
// 		when(shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId))
// 			.thenReturn(Optional.of(shippingAgent));
//
// 		boolean result = shippingAgentService.deleteShippingAgentForUser(shippingManagerId);
//
// 		assertFalse(result);
// 		verify(shippingAgentRepository, never()).save(any(ShippingAgent.class));
// 	}
// }
