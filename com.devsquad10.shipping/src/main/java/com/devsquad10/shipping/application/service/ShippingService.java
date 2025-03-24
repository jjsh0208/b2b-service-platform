package com.devsquad10.shipping.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.shipping.application.dto.MinimumCountAllocationResult;
import com.devsquad10.shipping.application.dto.request.ShippingSearchReqDto;
import com.devsquad10.shipping.application.dto.request.ShippingUpdateReqDto;
import com.devsquad10.shipping.application.dto.response.PagedShippingItemResDto;
import com.devsquad10.shipping.application.dto.response.PagedShippingResDto;
import com.devsquad10.shipping.application.dto.response.ShippingResDto;
import com.devsquad10.shipping.application.exception.shipping.ShippingNotFoundException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentAlreadyAllocatedException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentNotAllocatedException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentNotFoundException;
import com.devsquad10.shipping.application.service.allocation.ShippingAgentAllocation;
import com.devsquad10.shipping.domain.enums.ShippingStatus;
import com.devsquad10.shipping.domain.model.Shipping;
import com.devsquad10.shipping.domain.model.ShippingAgent;
import com.devsquad10.shipping.domain.model.ShippingHistory;
import com.devsquad10.shipping.domain.repository.ShippingAgentRepository;
import com.devsquad10.shipping.domain.repository.ShippingHistoryRepository;
import com.devsquad10.shipping.domain.repository.ShippingRepository;
import com.devsquad10.shipping.infrastructure.client.HubClient;
import com.devsquad10.shipping.infrastructure.client.HubRouteClient;
import com.devsquad10.shipping.infrastructure.client.MessageClient;
import com.devsquad10.shipping.infrastructure.client.OrderClient;
import com.devsquad10.shipping.infrastructure.client.UserClient;
import com.devsquad10.shipping.infrastructure.client.dto.HubFeignClientGetRequest;
import com.devsquad10.shipping.infrastructure.client.dto.OrderFeignClientDto;
import com.devsquad10.shipping.infrastructure.client.dto.ShippingClientData;
import com.devsquad10.shipping.infrastructure.client.dto.UserInfoFeignClientResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShippingService {

	private final ShippingRepository shippingRepository;
	private final ShippingHistoryRepository shippingHistoryRepository;
	private final ShippingAgentRepository shippingAgentRepository;
	private final ShippingAgentAllocation shippingAgentAllocation;
	private final HubClient hubClient;
	private final HubRouteClient hubRouteClient;
	private final UserClient userClient;
	private final OrderClient orderClient;
	private final MessageClient messageClient;

	// TODO: 권한 확인 - MASTER, 담당 HUB, DVL_AGENT
	//TODO: GPS + Geolocation 적용하여 배송 위치 추적에 따른 배송 경로기록 상태 이벤트 처리
	// 그 결과를 바로 배송 상태로 update 처리
	@Caching(
		put = {@CachePut(value = "shippingCache", key = "#id.toString", condition = "#id != null")},
		evict = {@CacheEvict(cacheNames = "shippingSearchCache", allEntries = true)}
	)
	public ShippingResDto statusUpdateShipping(UUID id, ShippingUpdateReqDto shippingUpdateReqDto) {
		Shipping shipping = shippingRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ShippingNotFoundException("ID " + id + "에 해당하는 배송 데이터를 찾을 수 없습니다."));

		shipping.preUpdate();
		return shippingRepository.save(shipping.toBuilder()
			.status(shippingUpdateReqDto.getStatus())
			.build()).toResponseDto();
	}

	// TODO: 배송 상태(HUB_ARV)가 되면 이벤트 처리로, 업체 배송담당자 할당(companyShippingManagerId update)
	@Caching(
		put = {@CachePut(value = "shippingCache", key = "#id.toString", condition = "#id != null")},
		evict = {@CacheEvict(cacheNames = "shippingSearchCache", allEntries = true)}
	)
	//TODO: 배송 담당자 배정 처리(주문 생성 전송시간 기준으로 허브간 이동이 시작되었다고 가정)
	// 전송시간+예상소요시간 기준
	// 배송 경로기록 마지막 순번의 현재상태가 "목적지 허브 도착:HUB_ARV"일 때만 배정 가능
	public ShippingResDto allocationShipping(UUID id) {
		// 동시성 처리로 인해 비관적 락 적용하여 동시성 제어
		Shipping shipping = shippingRepository.findByIdWithPessimisticLock(id)
			.orElseThrow(() -> new ShippingNotFoundException("ID " + id + "에 해당하는 배송 데이터를 찾을 수 없습니다."));

		// 배송담당자 ID가 이미 배정된 경우 처리
		if (shipping.getCompanyShippingManagerId() != null) {
			throw new ShippingAgentAlreadyAllocatedException("업체 배송담당자가 이미 배정되어 담당자배정 불가합니다.");
		}
		// 배정 횟수 컬럼 추가하여 배정 시 횟수 업데이트 구현 -> 최소 배정 건수인 배송담당자 선택
		MinimumCountAllocationResult allocationResult = shippingAgentAllocation.allocateCompanyAgent(
			shipping.getDestinationHubId(),
			shipping.getStatus()
		);
		if(allocationResult == null) {
			throw new ShippingAgentNotAllocatedException("배송 담당자 배정이 불가능합니다.");
		}

		// TODO: 테스트 후, 삭제(함께 슬랙 메시지 발송)
		sendSlackNotification(shipping.getOrderId());

		shipping.preUpdate();
		return shippingRepository.save(shipping.toBuilder()
			.companyShippingManagerId(allocationResult.getShippingManagerId())
			.build()).toResponseDto();
	}

	private void sendSlackNotification(UUID orderId) {
		messageClient.generateAndSendShippingTimeMessage(orderId);
	}


	// TODO: 권한 확인 - ALL + 담당 HUB, DVL_AGENT
	@Transactional(readOnly = true)
	@Cacheable(value = "shippingCache", key = "#id.toString()", condition = "#id != null")
	public ShippingResDto getShippingById(UUID id) {
		return shippingRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ShippingNotFoundException(id + " 해당하는 배송 ID가 존재하지 않습니다."))
			.toResponseDto();
	}

	// TODO: 권한 확인 - ALL + 담당 HUB, DVL_AGENT
	@Transactional(readOnly = true)
	@Cacheable(value = "shippingSearchCache",
		key = "{#request.id, #request.status != null ? #request.status.name() : 'null', #request.departureHubId, #request.destinationHubId, #request.companyShippingManagerId, "
			+ "#request.page, #request.size, #request.sortOption?.name(), #request.sortOrder?.name()}"
	)
	public PagedShippingResDto searchShipping(ShippingSearchReqDto request) {
		Page<Shipping> shippingPage = shippingRepository.findAll(request);

		Page<PagedShippingItemResDto> shippingItemResDtoPage = shippingPage
			.map(PagedShippingItemResDto::toResponse);

		return PagedShippingResDto.toResponseDto(shippingItemResDtoPage, request.getSortOption());
	}

	// TODO: 권한 확인 - MASTER, 담당 HUB
	@Caching(
		evict = {@CacheEvict(value = "shippingSearchCache", allEntries = true)}
	)
	public boolean deleteShippingForOrder(UUID orderId) {
		Shipping shipping = shippingRepository.findByOrderIdAndDeletedAtIsNull(orderId)
			.orElseThrow(() -> new ShippingNotFoundException("배송 내역에서 해당하는 주문 ID: " + orderId + "가 존재하지 않습니다."));

		// 배송 상태가 HUB_WAIT(허브 대기 중) 경우만 삭제 가능
		if(shipping.getStatus() != ShippingStatus.HUB_WAIT) {
			log.info(shipping.getStatus() + " 상태는 배송 취소가 불가능합니다.");
			return false;
		} else {
			// 배송 ID로 배송경로기록 List 추출
			List<ShippingHistory> historyList = shippingHistoryRepository.findByShippingIdAndDeletedAtIsNull(shipping.getId());

			// 배송 삭제 될 때, 배송 경로기록도 삭제 처리
			if (!historyList.isEmpty()) {
				List<UUID> historyIdList = historyList.stream()
					.map(ShippingHistory::getId)
					.toList();
				for (UUID historyId : historyIdList) {
					ShippingHistory history = shippingHistoryRepository.findByIdAndDeletedAtIsNull(historyId);
					shippingHistoryRepository.save(history.softDelete());
					// 허브간 배송이 모두 1명의 허브배송 담당자로 배정했다는 가정하에 배송 경로삭제 시, 허브 담당자 isTransit=false 로 변경
					UUID shippingManagerId = history.getShippingManagerId();
					ShippingAgent selectedAgent = shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId)
						.orElseThrow(() -> new ShippingAgentNotFoundException("배송 담당자 id 가 존재하지 않습니다."));
					if(selectedAgent.getIsTransit()) {
						selectedAgent.isTransitToFalse();
						selectedAgent.decreaseAssignmentCount();
						shippingAgentRepository.save(selectedAgent);
					}
				}
			}
			// 배송 삭제 처리
			shippingRepository.save(shipping.softDelete());
			return true;
		}
	}

	// AI API 배송 데이터 검증
	public Boolean isShippingDataExists(UUID orderId) {
		Shipping shipping = shippingRepository.findByOrderIdAndDeletedAtIsNull(orderId)
			.orElseThrow(() -> new ShippingNotFoundException("배송 내역에서 해당하는 주문 ID: " + orderId + "가 존재하지 않습니다."));
		if(shipping == null) {
			return false;
		} else {
			return true;
		}
	}

	// AI 슬랙 알림 전송용 배송 데이터 요청
	public ShippingClientData getShippingClientData(UUID orderId) {
		Shipping shipping = shippingRepository.findByOrderIdAndDeletedAtIsNull(orderId)
			.orElseThrow(() -> new ShippingNotFoundException("배송 내역에서 해당하는 주문 ID: " + orderId + "가 존재하지 않습니다."));

		List<HubFeignClientGetRequest> getHubRoutes = hubRouteClient.getHubRouteInfo(
			shipping.getDepartureHubId(),
			shipping.getDestinationHubId()
		);
		getHubRoutes.sort(Comparator.comparing(HubFeignClientGetRequest::getSequence));

		List<UUID> waypoints = new ArrayList<>();
		for (HubFeignClientGetRequest hubRoute : getHubRoutes) {
			waypoints.add(hubRoute.getDestinationHubId());
		}

		// 허브Id로 "허브명" 조회하는 Hub feign client 요청
		String departureHubName = hubClient.getHubName(shipping.getDepartureHubId());
		String destinationHubName = hubClient.getHubName(shipping.getDestinationHubId());

		List<String> waypointNames = new ArrayList<>();
		for(UUID waypointId : waypoints) {
			String waypointName = hubClient.getHubName(waypointId);
			waypointNames.add(waypointName);
		}

		// 주문Id로 "상품명, 수량" 조회하는 Order feign client 요청
		OrderFeignClientDto getOrder = orderClient.getOrderProductDetails(orderId);

		// 배송담당자 id로 "이름, 슬랙ID" 정보 조회하는 User feign client 요청
		UserInfoFeignClientResponse shippingManagerInfo = userClient.getUserInfoRequest(shipping.getCompanyShippingManagerId());

		return ShippingClientData.builder()
			.orderId(shipping.getOrderId())
			.customerName(shipping.getRecipientName())
			.productInfo(getOrder.getProductName())
			.quantity(getOrder.getQuantity())
			.requestDetails(shipping.getRequestDetails())
			.departureHubName(departureHubName)
			.destinationHubName(destinationHubName)
			.waypointHubNames(waypointNames)
			.shippingManagerName(shippingManagerInfo.getUsername())
			.shippingManagerSlackId(shippingManagerInfo.getSlackId())
			.build();
	}
}
