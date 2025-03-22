package com.devsquad10.shipping.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.shipping.application.dto.response.ShippingAgentResDto;
import com.devsquad10.shipping.application.exception.shippingAgent.HubIdNotFoundException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentNotFoundException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentNotUpdateException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentTypeNotFoundException;
import com.devsquad10.shipping.domain.enums.ShippingAgentType;
import com.devsquad10.shipping.domain.model.ShippingAgent;
import com.devsquad10.shipping.domain.repository.ShippingAgentRepository;
import com.devsquad10.shipping.infrastructure.client.HubClient;
import com.devsquad10.shipping.infrastructure.client.ShippingAgentFeignClientPatchRequest;
import com.devsquad10.shipping.infrastructure.client.ShippingAgentFeignClientPostRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShippingAgentService {

	private final HubClient hubClient;
	private final ShippingAgentRepository shippingAgentRepository;

	public void createShippingAgent(@Valid ShippingAgentFeignClientPostRequest request) {

		//TODO: User 정보 feign client 로 받기
		// 권한 확인 - MASTER, 담당 HUB
		UUID reqShippingManagerId = request.getId(); // 배송담당자 ID
		String reqSlackId = request.getSlackId();

		// 담당자 타입 존재 유효성 검사
		// TODO: COM_DVL 경우, 소속 허브 ID 유효성 검사 필요
		ShippingAgentType reqType = request.getType();
		if(reqType != ShippingAgentType.HUB_DVL && reqType != ShippingAgentType.COM_DVL) {
			throw new ShippingAgentTypeNotFoundException(reqType + " Shipping Agent type is not supported");
		}

		// HubId 존재 유효성 검사
		UUID reqHubId = request.getHubId();
		if(reqType == ShippingAgentType.COM_DVL) {
			// HubId 존재 유무 feign client 호출
			if(!hubClient.isHubExists(reqHubId)) {
				throw new HubIdNotFoundException("Hub id " + reqHubId + " not found");
			}
		}

		// 순차적 순번 배정을 위한 최대값 추출 및 다음 순번 처리
		Optional<Integer> maxSequence = shippingAgentRepository.findMaxShippingSequence();
		Integer nextSequence = maxSequence.map(s -> ++s).orElse(1);
		log.info("nextSequence : {}", nextSequence);

		shippingAgentRepository.save(ShippingAgent.builder()
			.shippingManagerId(reqShippingManagerId)
			.hubId(reqHubId)
			.shippingManagerSlackId(reqSlackId)
			.type(request.getType())
			.shippingSequence(nextSequence)
			.isTransit(false)
			.build()
		);
	}

	// TODO: 권한 확인 - MASTER, 담당 HUB, 담당 DLV_AGENT
	@Transactional(readOnly = true)
	public ShippingAgentResDto getShippingAgentById(UUID id) {

		ShippingAgent targetshippingAgent = shippingAgentRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ShippingAgentNotFoundException(id + ": 배송 관리자 ID가 존재하지 않습니다."));

		return ShippingAgent.builder()
			.id(targetshippingAgent.getId())
			.hubId(targetshippingAgent.getHubId())
			.shippingManagerId(targetshippingAgent.getShippingManagerId())
			.shippingManagerSlackId(targetshippingAgent.getShippingManagerSlackId())
			.type(targetshippingAgent.getType())
			.shippingSequence(targetshippingAgent.getShippingSequence())
			.isTransit(targetshippingAgent.getIsTransit())
			.build()
			.toResponse();
	}

	// TODO: 권한 확인 - MASTER, 담당 HUB, 담당 DLV_AGENT
	// @Transactional(readOnly = true)
	// public Page<ShippingAgentResDto> searchShippingAgents(
	// 	String query, String category,
	// 	Pageable pageable
	// 	// int page, int size, String sortBy, String orderBy
	// ) {
	//
	// 	// pageable.getSort().stream().forEach(sort -> {
	// 	// 	String property = sort.getProperty();
	// 	// 	Sort.Order order = sort.isAscending() ? Sort.Order.asc(property) : Sort.Order.desc(property);
	// 	//
	// 	// 	Path<Object> target = Expressions.path(Object.class, QShippingAgent.shippingAgent, property);
	// 	// 	OrderSpecifier<?> orderSpecifier = new OrderSpecifier(sort, target);
	// 	// 	query.orderBy(orderSpecifier);
	// 	// });
	// 	// return null;
	//
	// 	Page<ShippingAgent> shippingAgentPage = shippingAgentRepository
	// 		.findAll(query, category, pageable);
	// 	// Page<ShippingAgentResDto> shippingAgentResDtoPage = shippingAgentPage
	// 	// 	.map(ShippingAgentResDto::toResponse);
	// 	return shippingAgentPage
	// 		.map(ShippingAgentResDto::toResponse);
	// }

	//TODO: 권한 확인 - MASTER, 담당HUB
	// 1.유저 feign client 호출하여 넘겨받은 정보 변경
	// 삭제된 배송담당자ID 경우, internal server error 발생
	public void infoUpdateShippingAgent(
		ShippingAgentFeignClientPatchRequest request) {

		// shippingId 유효성 검사
		ShippingAgent target = shippingAgentRepository
			.findByShippingManagerIdAndDeletedAtIsNull(
				request.getShippingManagerId())
			.orElseThrow(() ->
				new ShippingAgentNotFoundException(
					"배송 관리자 ID:" + request.getShippingManagerId()  + "가 존재하지 않습니다."));

		// hubId 유효성 검사
		if(request.getHubId() != null) {
			if(!hubClient.isHubExists(request.getHubId())) {
				throw new HubIdNotFoundException("Hub id " + request.getHubId() + " not found");
			}
		}

		target.preUpdate();
		shippingAgentRepository.save(target.toBuilder()
			.shippingManagerId(request.getShippingManagerId())
			.hubId(request.getHubId())
			.shippingManagerSlackId(request.getSlackId())
			.build())
			.toResponse();
	}

	// TODO: 권한 확인 - MASTER, 담당HUB
	// 2.배송 여부 확인 변경
	public ShippingAgentResDto transitUpdateShippingAgent(
		UUID id,
		Boolean isTransit
	) {
		log.info("isTransit: {}", isTransit);
		ShippingAgent target = shippingAgentRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ShippingAgentNotFoundException(id + ": 배송 관리자 ID가 존재하지 않습니다."));
		if(isTransit == target.getIsTransit()) {
			throw new ShippingAgentNotUpdateException(isTransit + ": 배송여부가 동일하므로 수정되지 않았습니다.");
		}
		target.preUpdate();
		return shippingAgentRepository.save(target.toBuilder()
				.isTransit(isTransit)
				.build())
			.toResponse();
	}

	// TODO: 삭제도 User feign client 호출로 처리
	// TODO: 권한 확인 - MASTER, 담당HUB
	public void deleteShippingAgent(UUID id) {
		ShippingAgent target = shippingAgentRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ShippingAgentNotFoundException(id + ": 배송 관리자 ID가 존재하지 않습니다."));

		shippingAgentRepository.save(target.softDelete());
	}

	// TODO: 담당자 배정 로직 구현은 새로운 서비스 생성하고
	//  더미데이터 180명 query 만들어서 구현!
}
