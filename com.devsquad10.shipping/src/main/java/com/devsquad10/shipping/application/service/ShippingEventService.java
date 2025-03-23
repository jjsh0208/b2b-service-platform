package com.devsquad10.shipping.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.shipping.application.dto.message.ShippingCreateMessage;
import com.devsquad10.shipping.application.dto.message.ShippingResponseMessage;
import com.devsquad10.shipping.application.dto.message.ShippingUpdateMessage;
import com.devsquad10.shipping.application.exception.shipping.ShippingCreateException;
import com.devsquad10.shipping.application.exception.shipping.ShippingNotFoundException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentNotFoundException;
import com.devsquad10.shipping.application.service.allocation.ShippingAgentAllocation;
import com.devsquad10.shipping.application.service.message.ShippingMessageService;
import com.devsquad10.shipping.domain.enums.ShippingHistoryStatus;
import com.devsquad10.shipping.domain.enums.ShippingStatus;
import com.devsquad10.shipping.domain.model.Shipping;
import com.devsquad10.shipping.domain.model.ShippingAgent;
import com.devsquad10.shipping.domain.model.ShippingHistory;
import com.devsquad10.shipping.domain.repository.ShippingAgentRepository;
import com.devsquad10.shipping.domain.repository.ShippingHistoryRepository;
import com.devsquad10.shipping.domain.repository.ShippingRepository;
import com.devsquad10.shipping.infrastructure.client.CompanyClient;
import com.devsquad10.shipping.infrastructure.client.HubClient;
import com.devsquad10.shipping.infrastructure.client.HubFeignClientGetRequest;
import com.devsquad10.shipping.infrastructure.client.ShippingCompanyInfoDto;
import com.devsquad10.shipping.infrastructure.client.UserClient;
import com.devsquad10.shipping.infrastructure.client.UserInfoFeignClientRequest;
import com.fasterxml.jackson.core.JsonProcessingException;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ShippingEventService {

	private final ShippingRepository shippingRepository;
	private final ShippingHistoryRepository shippingHistoryRepository;
	private final ShippingAgentRepository shippingAgentRepository;
	private final ShippingAgentAllocation shippingAgentAllocation;
	private final ShippingMessageService shippingMessageService;
	private final HubClient hubClient;
	private final CompanyClient companyClient;
	private final UserClient userClient;

	// TODO: 권한 확인 - MASTER
	// 배송 & 배송 경로 기록 생성
	public void handlerShippingCreateRequest(ShippingCreateMessage shippingCreateMessage) throws
		JsonProcessingException {
		// 주문:reqMessage(주문Id,공급업체,수령업체,주소,요청사항,납기일자)
		// company feign Client로 공급업체 정보 조회
		ShippingCompanyInfoDto supplierIdInfo = getSupplierInfo(shippingCreateMessage.getSupplierId(),
			shippingCreateMessage);

		// company feign Client로 수령업체 정보 조회
		ShippingCompanyInfoDto recipientsInfo = getRecipientsInfo(shippingCreateMessage.getRecipientsId(),
			shippingCreateMessage);

		// 도착허브Id에서 수령업체의 담당자ID로 User feign client 이름 조회하여 수령인 이름 및 슬랙Id 추출
		UserInfoFeignClientRequest userInfo = userClient.getUserInfoRequest(recipientsInfo.getVenderId());

		Shipping shipping = Shipping.builder()
			.status(ShippingStatus.HUB_WAIT)
			.departureHubId(supplierIdInfo.getHubId())
			.destinationHubId(recipientsInfo.getHubId())
			.orderId(shippingCreateMessage.getOrderId())
			.address(shippingCreateMessage.getAddress())
			.requestDetails(
				shippingCreateMessage.getRequestDetails() != null ? shippingCreateMessage.getRequestDetails() : "")
			.recipientName(userInfo.getUsername())
			.recipientSlackId(userInfo.getSlackId())
			// TODO: shipping의 status가 HUB_ARV 될때 event 발생하여 업체 배송담당자 배정처리
			.companyShippingManagerId(null)
			.deadLine(shippingCreateMessage.getDeadLine())
			.build();
		Shipping savedShipping = shippingRepository.save(shipping);
		log.info("savedShipping: {}", savedShipping);

		// 배송 경로기록 생성: 허브간 이동정보 feign client 매개변수(출발/도착허브 ID)와 일치하는 예상거리, 소요시간, 경유지(List) 추출
		// 허브간 이동정보(hub-to relay-hub) 구현 시, feign client 호출하여 허브 순번대로 shippingHistory 생성
		log.info("허브 feign client 허브 경로 정보 조회 전");
		List<HubFeignClientGetRequest> hubRouteInfo = getHubRouteInfo(supplierIdInfo, recipientsInfo, shippingCreateMessage);
		log.info("허브 feign client 허브 경로 정보 조회 후");
		// 허브간 경로이동 생성 전, 허브 배송담당자 배정
		UUID selectedHubShippingAgentId = allocationHubShippingManagerId(recipientsInfo.getHubId());

		// 배송 경로기록 생성 및 저장
		createShippingHistory(hubRouteInfo, savedShipping, selectedHubShippingAgentId);

		// 배송/배송경로기록 생성 완료 -> 주문에 전달할 response
		try {
			log.info("배송,배송경로기록 생성 완료로 주문 메시지 발행");
			shippingMessageService.sendShippingCreateMessage(savedShipping.toShippingResponseMessage());
		} catch (Exception e) {
			log.error("배송 생성 실패: {}", e.getMessage());
			failErrorMessage(shippingCreateMessage);
			throw new RuntimeException("배송 생성 실패: " + e.getMessage(), e);
		}
	}

	// 배송생성 실패 시, status FAIL 전송하여 주문생성 롤백 처리 전달!
	public void sendShippingCreateRollbackMessage(ShippingResponseMessage rollbackMessage) {
		log.info("배송 생성 예외 발생으로 롤백 메시지 발행");
		shippingMessageService.sendShippingCreateRollbackMessage(rollbackMessage);
		throw new ShippingCreateException("배송 생성 실패");
	}

	// 배송 생성 실패 메시지 전송
	private void failErrorMessage(ShippingCreateMessage shippingCreateMessage) {
		ShippingResponseMessage rollbackMessage = new ShippingResponseMessage();
		rollbackMessage.setOrderId(shippingCreateMessage.getOrderId());
		rollbackMessage.setStatus("FAIL");
		// 배송 생성 실패 시 보상 트랜잭션 메시지 발행 요청
		sendShippingCreateRollbackMessage(rollbackMessage);
	}

	// company feign Client로 공급업체 정보 조회
	private ShippingCompanyInfoDto getSupplierInfo(UUID supplierId, ShippingCreateMessage shippingCreateMessage) {
		try {
			return companyClient.findShippingCompanyInfo(supplierId);
		} catch (FeignException.FeignClientException e) {
			log.error("업체 feign client 호출 실패로 출발허브 ID 조회 불가");
			failErrorMessage(shippingCreateMessage);
			throw new ShippingCreateException("배송 생성 실패");
		}
	}

	// company feign Client로 수령업체 정보 조회
	private ShippingCompanyInfoDto getRecipientsInfo(UUID recipientsId, ShippingCreateMessage shippingCreateMessage) {
		try {
			return companyClient.findShippingCompanyInfo(recipientsId);
		} catch (FeignException.FeignClientException e) {
			log.error("업체 feign client 호출 실패로 도착허브 정보 조회 불가");
			failErrorMessage(shippingCreateMessage);
			throw new ShippingCreateException("배송 생성 실패");
		}
	}

	// 허브 feign client 호출하여 허브 이동 경로 조회
	private List<HubFeignClientGetRequest> getHubRouteInfo(
		ShippingCompanyInfoDto supplierIdInfo,
		ShippingCompanyInfoDto recipientsInfo,
		ShippingCreateMessage shippingCreateMessage) {

		try {
			List<HubFeignClientGetRequest> hubRouteInfo = hubClient.getHubRouteInfo(supplierIdInfo.getHubId(),
				recipientsInfo.getHubId());
			log.info("husRouteInfo.size(): {}", hubRouteInfo.size());

			if (hubRouteInfo.isEmpty()) {
				log.error("허브간 이동정보가 존재하지 않습니다.");
				failErrorMessage(shippingCreateMessage);
				throw new EntityNotFoundException("허브간 이동정보가 존재하지 않습니다.");
			}
			hubRouteInfo.sort(Comparator.comparingInt(HubFeignClientGetRequest::getSequence));
			return hubRouteInfo;

		} catch(FeignException.FeignClientException e) {
			log.error("허브 feign client 호출 실패로 허브간 이동정보 조회 불가");
			failErrorMessage(shippingCreateMessage);
			throw new ShippingCreateException("배송 생성 실패");
		}
	}


	// 허브간 경로이동 생성 전, 허브 배송담당자 배정
	private UUID allocationHubShippingManagerId(UUID destinationHubId) {
		UUID selectedHubShippingAgentId = shippingAgentAllocation
			.allocateHubAgent(destinationHubId)
			.getShippingManagerId();
		if (selectedHubShippingAgentId == null) {
			log.error("배정 가능한 허브배송담당자가 존재하지 않습니다.");
			throw new EntityNotFoundException("배정 가능한 허브배송담당자가 존재하지 않습니다");
		}
		return selectedHubShippingAgentId;
	}

	// 배송 경로기록 생성 및 저장
	private void createShippingHistory(
		List<HubFeignClientGetRequest> hubRouteInfo,
		Shipping savedShipping,
		UUID selectedHubShippingAgentId) {

		List<ShippingHistory> shippingHistories = new ArrayList<>();

		for(HubFeignClientGetRequest route : hubRouteInfo) {
			ShippingHistory shippingHistory = ShippingHistory.builder()
				.shipping(savedShipping)
				.shippingPathSequence(route.getSequence())
				.departureHubId(route.getDepartureHubId())
				.destinationHubId(route.getDestinationHubId())
				.shippingManagerId(selectedHubShippingAgentId)
				.estDist(route.getDistance())
				.estTime(route.getTime())
				// TODO: 실제 거리 및 시간 계산은 현재 위치 기반으로 정보를 수집하여 update 처리
				.actDist(route.getDistance() + 2.23)
				.actTime(route.getTime() + 2342365)
				.historyStatus(ShippingHistoryStatus.HUB_WAIT)
				.build();
			shippingHistories.add(shippingHistory);
			shippingHistoryRepository.save(shippingHistory);
		}
	}


	// 배송 & 배송 경로 기록 수정
	public void handlerShippingUpdateRequest(ShippingUpdateMessage shippingUpdateMessage) {
		Shipping shipping = shippingRepository.findByOrderIdAndDeletedAtIsNull(shippingUpdateMessage.getOrderId())
			.orElseThrow(() -> new ShippingNotFoundException("배송 내역에서 해당하는 주문 ID: " + shippingUpdateMessage.getOrderId() + "가 존재하지 않습니다."));

		// 배송 상태가 HUB_WAIT(허브 대기 중) 경우만 삭제 가능
		if(shipping.getStatus() != ShippingStatus.HUB_WAIT) {
			log.info(shipping.getStatus() + " 상태는 배송 수정이 불가능합니다.");
			failErrorMessage(shippingUpdateMessage);
		}

		// 출발 허브Id 추출
		UUID departureHubId = shipping.getDepartureHubId();

		// company feign Client로 수령업체 정보 조회
		UUID recipientsId = shippingUpdateMessage.getRecipientsId();
		ShippingCompanyInfoDto recipientsInfo = getRecipientsInfo(recipientsId, shippingUpdateMessage);

		// 도착허브Id에서 수령업체의 담당자ID로 User feign client 이름 조회하여 수령인 이름 및 슬랙Id 추출
		UserInfoFeignClientRequest userInfo = getUserInfo(recipientsInfo, shippingUpdateMessage);

		// 기존 배송 경로 기록 삭제 및 허브배송담당자 배송진행 여부(false) 변경 & 배정 횟수(assignmentCount--) 롤백
		List<ShippingHistory> shippingHistories = shippingHistoryRepository.findByShippingIdAndDeletedAtIsNull(shipping.getId());
		for(ShippingHistory history : shippingHistories) {
			shippingHistoryRepository.save(history.softDelete());
			UUID managerId = history.getShippingManagerId();
			ShippingAgent selectedAgent = shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(managerId)
				.orElseThrow(() -> new ShippingAgentNotFoundException("배송 담당자가 존재하지 않습니다."));
			if(selectedAgent.getIsTransit()) {
				selectedAgent.isTransitToFalse();
				selectedAgent.decreaseAssignmentCount();
			}
		}

		// 배송 경로기록 생성: 허브간 이동정보 feign client 매개변수(출발/도착허브 ID)와 일치하는 예상거리, 소요시간, 경유지(List) 추출
		// 허브간 이동정보(hub-to relay-hub) 구현 시, feign client 호출하여 허브 순번대로 shippingHistory 생성
		log.info("허브 feign client 호출하여 새로운 허브 경로 정보 조회 전");
		List<HubFeignClientGetRequest> hubRouteInfo = getHubRouteInfo(departureHubId, recipientsInfo, shippingUpdateMessage);
		log.info("허브 feign client 호출하여 새로운 허브 경로 정보 조회 후");

		// 허브간 경로이동 생성 전, 허브 배송담당자 배정
		UUID selectedHubShippingAgentId = allocationHubShippingManagerId(recipientsInfo.getHubId());

		log.info("배송 수정 전");
		shipping.preUpdate();
		Shipping updatedShipping = shippingRepository.save(shipping.toBuilder()
			.orderId(shippingUpdateMessage.getOrderId())
			.destinationHubId(recipientsInfo.getHubId())
			.address(shippingUpdateMessage.getAddress())
			.recipientName(userInfo.getUsername())
			.recipientSlackId(userInfo.getSlackId())
			.requestDetails(shippingUpdateMessage.getRequestDetails())
			.deadLine(shippingUpdateMessage.getDeadLine())
			.build());
		log.info("배송 수정 후");
		// 새로운 배송 경로기록 생성 및 저장
		log.info("새로운 배송 경로기록 생성 전");
		createShippingHistory(hubRouteInfo, updatedShipping, selectedHubShippingAgentId);
		log.info("새로운 배송 경로기록 생성 후");
		// 배송 정보 수정에 따른 배송경로기록 삭제/수정 완료 후 -> 주문에 전달할 response
		try {
			log.info("배송,배송경로기록 수정 후 주문 메시지 발행");
			shippingMessageService.sendUpdateOrderAndShippingDetails(updatedShipping.toShippingResponseMessage());
		} catch (Exception e) {
			log.warn("배송 수정 실패: {}", e.getMessage());
			failErrorMessage(shippingUpdateMessage);
			throw new RuntimeException("배송 수정 실패: " + e.getMessage(), e);
		}
	}

	// 배송수정 실패 시, status FAIL 전송하여 주문수정 롤백 처리 전달!
	public void sendShippingUpdateRollbackMessage(ShippingResponseMessage rollbackMessage) {
		log.info("배송 수정 중 예외 발생으로 롤백 메시지 발행");
		shippingMessageService.sendUpdateOrderAndShippingDetailsRollbackMessage(rollbackMessage);
		throw new ShippingCreateException("배송 생성 실패");
	}

	// 배송 생성 실패 메시지 전송
	private void failErrorMessage(ShippingUpdateMessage shippingUpdateMessage) {
		ShippingResponseMessage rollbackMessage = new ShippingResponseMessage();
		rollbackMessage.setOrderId(shippingUpdateMessage.getOrderId());
		rollbackMessage.setStatus("FAIL");
		// 배송 수정 실패 시 보상 트랜잭션 메시지 발행 요청
		sendShippingUpdateRollbackMessage(rollbackMessage);
	}

	// company feign Client로 수령업체 정보 조회
	private ShippingCompanyInfoDto getRecipientsInfo(UUID recipientsId, ShippingUpdateMessage shippingUpdateMessage) {
		try {
			return companyClient.findShippingCompanyInfo(recipientsId);
		} catch (FeignException.FeignClientException e) {
			log.error("업체 feign client 호출 실패로 인해 도착허브 정보 조회 불가");
			failErrorMessage(shippingUpdateMessage);
			throw new ShippingCreateException("배송 수정 실패");
		}
	}

	// 도착허브Id에서 수령업체의 담당자ID로 User feign client 이름 조회하여 수령인 이름 및 슬랙Id 추출
	private UserInfoFeignClientRequest getUserInfo(ShippingCompanyInfoDto recipientsInfo, ShippingUpdateMessage shippingUpdateMessage) {
		try {
			return userClient.getUserInfoRequest(recipientsInfo.getVenderId());
		} catch (FeignException.FeignClientException e) {
			log.error("유저 feign client 호출 실패로 사용자 정보 조회 불가");
			failErrorMessage(shippingUpdateMessage);
			throw new ShippingCreateException("배송 수정 실패");
		}
	}

	// 허브 feign client 호출하여 허브 이동 경로 조회
	private List<HubFeignClientGetRequest> getHubRouteInfo(
		UUID departureHubId,
		ShippingCompanyInfoDto recipientsInfo,
		ShippingUpdateMessage shippingUpdateMessage) {

		try {
			List<HubFeignClientGetRequest> hubRouteUpdateInfo = hubClient.getHubRouteInfo(departureHubId,
				recipientsInfo.getHubId());
			log.info("hubRouteUpdateInfo.size(): {}", hubRouteUpdateInfo.size());

			if (hubRouteUpdateInfo.isEmpty()) {
				log.warn("허브간 이동정보가 존재하지 않습니다.");
				failErrorMessage(shippingUpdateMessage);
				throw new EntityNotFoundException("허브간 이동정보가 존재하지 않습니다.");
			}
			hubRouteUpdateInfo.sort(Comparator.comparingInt(HubFeignClientGetRequest::getSequence));
			return hubRouteUpdateInfo;
		} catch(FeignException.FeignClientException e) {
			log.error("허브 feign client 호출 실패로 새로운 허브간 이동정보 조회 불가");
			failErrorMessage(shippingUpdateMessage);
			throw new ShippingCreateException("배송 수정 실패");
		}
	}

}
