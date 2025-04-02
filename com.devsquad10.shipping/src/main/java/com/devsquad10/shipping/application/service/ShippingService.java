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
import com.devsquad10.shipping.application.exception.shipping.InvalidShippingStatusUpdateException;
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
import com.devsquad10.shipping.infrastructure.client.dto.ShippingClientDataRequestDto;
import com.devsquad10.shipping.infrastructure.client.dto.ShippingClientDataResponseDto;
import com.devsquad10.shipping.infrastructure.client.dto.UserInfoFeignClientResponse;

import jakarta.persistence.EntityNotFoundException;
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

	// 권한 - MASTER, 담당 HUB, DVL_AGENT
	// TODO: GPS + Geolocation 적용하여 배송 위치 추적에 따른 배송 경로기록 상태 이벤트 처리 예정
	// 배송 상태(HUB_WAIT -> HUB_TRNS -> HUB_ARV -> COM_TRNS -> DLV_COMP)
	@Caching(
		put = {@CachePut(value = "shippingCache", key = "#id.toString", condition = "#id != null")},
		evict = {@CacheEvict(cacheNames = "shippingSearchCache", allEntries = true)}
	)
	public ShippingResDto statusUpdateShipping(UUID id, ShippingUpdateReqDto shippingUpdateReqDto, UUID userId) {
		// 동시성 처리로 인해 비관적 락 적용하여 동시성 제어
		Shipping shipping = shippingRepository.findByIdWithPessimisticLock(id)
			.orElseThrow(() -> new ShippingNotFoundException("ID " + id + "에 해당하는 배송 데이터를 찾을 수 없습니다."));

		// 배송 상태 변경 순서 정의
		ShippingStatus[] statusOrder = {
			ShippingStatus.HUB_WAIT,
			ShippingStatus.HUB_TRNS,
			ShippingStatus.HUB_ARV,
			ShippingStatus.COM_TRNS,
			ShippingStatus.DLV_CMP
		};

		// 현재 배송 상태의 순서
		int currentStatusIndex = -1;
		for (int i = 0; i < statusOrder.length; i++) {
			if (shipping.getStatus() == statusOrder[i]) {
				currentStatusIndex = i;
				break;
			}
		}

		// 요청된 배송 상태의 순서
		int requestedStatusIndex = -1;
		for (int i = 0; i < statusOrder.length; i++) {
			if (shippingUpdateReqDto.getStatus() == statusOrder[i]) {
				requestedStatusIndex = i;
				break;
			}
		}

		// 이전 상태로 업데이트 시도 시 예외 발생
		if (requestedStatusIndex < currentStatusIndex) {
			throw new InvalidShippingStatusUpdateException("이전 배송 상태로 업데이트할 수 없습니다.");
		}

		// 허브 대기 중 -> 허브 이동 중 : 주문 상태 'shipped'로 업데이트
		if (shipping.getStatus() == ShippingStatus.HUB_WAIT && shippingUpdateReqDto.getStatus() == ShippingStatus.HUB_TRNS) {
			log.info("id: {}, shipping.getId(): {}", id, shipping.getId());
			orderClient.updateOrderStatusToShipped(id);
			log.info("주문 상태 shipped로 변경 완료");
		}

		// 허브 도착 상태(HUB_ARV) 업데이트하면 업체배송담당자 할당 - ID update
		if(shippingUpdateReqDto.getStatus() == ShippingStatus.HUB_ARV) {
			ShippingResDto assignmentShipping = allocationShipping(id, shipping);
			if(assignmentShipping == null) {
				log.info("배정된 업체 배송 담당자 존재하지 않습니다.");
				throw new ShippingAgentNotAllocatedException("배정된 업체 배송 담당자 존재하지 않습니다.");
			}
			log.info("companyShippingManagerId: {}", assignmentShipping.getCompanyShippingManagerId());
		}

		shipping.preUpdate();
		return shippingRepository.save(shipping.toBuilder()
			.status(shippingUpdateReqDto.getStatus())
			.createdBy(userId)
			.build()).toResponseDto();
	}

	// 배송 경로기록 마지막 순번의 현재상태가 "목적지 허브 도착:HUB_ARV"일 때만 업체 배송담당자 배정됨.
	private ShippingResDto allocationShipping(UUID id, Shipping shipping) {

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

		shipping.preUpdate();
		return shippingRepository.save(shipping.toBuilder()
			.companyShippingManagerId(allocationResult.getShippingManagerId())
			.build()).toResponseDto();
	}

	// TODO: 테스트 후, 삭제(함께 슬랙 메시지 발송)
	// // 슬랙 발송 API 테스트
	// public ShippingClientDataResponseDto sendSlackMessage(UUID orderId) {
	// 	log.info("서비스 시작");
	// 	ShippingClientDataResponseDto responseDto = sendSlackNotification(orderId);
	// 	log.info("서비스 끝 OrderId: {}", responseDto.getRecipientId());
	// 	return responseDto;
	// }
	//
	// private ShippingClientDataResponseDto sendSlackNotification(UUID orderId) {
	// 	log.info("메시지 호출 전");
	// 	ShippingClientDataResponseDto response = messageClient.getShippingClientData(orderId);
	// 	log.info("메시지 호출 후");
	// 	if(response == null) {
	// 		throw new EntityNotFoundException("슬랙 메시지가 내용이 없습니다.");
	// 	}
	// 	return response;
	// }

	// 권한 - ALL + 담당 HUB, DVL_AGENT
	@Transactional(readOnly = true)
	@Cacheable(value = "shippingCache", key = "#id.toString()", condition = "#id != null")
	public ShippingResDto getShippingById(UUID id) {
		return shippingRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ShippingNotFoundException(id + " 해당하는 배송 ID가 존재하지 않습니다."))
			.toResponseDto();
	}

	// 권한 - ALL + 담당 HUB, DVL_AGENT
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

	// 권한 - MASTER, 담당 HUB
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
	public ShippingClientDataRequestDto getShippingClientData(UUID orderId) {
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
		log.info("shipping.getCompanyShippingManagerId(): {}", shipping.getCompanyShippingManagerId());
		UserInfoFeignClientResponse shippingManagerInfo = userClient.getUserInfoRequest(shipping.getCompanyShippingManagerId());

		return ShippingClientDataRequestDto.builder()
			.orderId(shipping.getOrderId())
			.customerName(shipping.getRecipientName())
			.productInfo(getOrder.getProductName())
			.quantity(getOrder.getQuantity())
			.requestDetails(shipping.getRequestDetails())
			.departureHubName(departureHubName)
			.waypointHubNames(waypointNames)
			.destinationHubName(destinationHubName)
			.address(shipping.getAddress())
			.shippingManagerName(shippingManagerInfo.getUsername())
			.build();
	}
}
