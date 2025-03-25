package com.devsquad10.shipping.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.shipping.application.dto.request.ShippingAgentSearchReqDto;
import com.devsquad10.shipping.application.dto.response.PagedShippingAgentItemResDto;
import com.devsquad10.shipping.application.dto.response.PagedShippingAgentResDto;
import com.devsquad10.shipping.application.dto.response.ShippingAgentResDto;

import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentNotFoundException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentNotUpdateException;

import com.devsquad10.shipping.domain.enums.ShippingAgentType;
import com.devsquad10.shipping.domain.model.ShippingAgent;
import com.devsquad10.shipping.domain.repository.ShippingAgentRepository;
import com.devsquad10.shipping.infrastructure.client.HubClient;
import com.devsquad10.shipping.infrastructure.client.dto.ShippingAgentFeignClientPatchRequest;
import com.devsquad10.shipping.infrastructure.client.dto.ShippingAgentFeignClientPostRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShippingAgentService {

	private final HubClient hubClient;
	private final ShippingAgentRepository shippingAgentRepository;

	// 권한 - MASTER, 담당 HUB
	@Caching(
		evict = {@CacheEvict(value = "shippingAgentSearchCache", allEntries = true)}
	)
	public boolean createShippingAgent(ShippingAgentFeignClientPostRequest request) {
		// User 정보 feign client 로 호출되어 생성됨
		UUID reqShippingManagerId = request.getShippingManagerId(); // 배송담당자 ID
		String reqSlackId = request.getSlackId();

		// 담당자 타입 존재 유효성 검사
		// COM_DVL 경우, 소속 허브 ID 유효성 검사 필요
		ShippingAgentType reqType = request.getType();
		if(reqType != ShippingAgentType.HUB_DVL && reqType != ShippingAgentType.COM_DVL) {
			log.error(reqType + " Shipping Agent type is not supported");
			return false;
		}

		// HubId 존재 유효성 검사
		UUID reqHubId = request.getHubId();
		if(reqType == ShippingAgentType.COM_DVL) {
			// HubId 존재 유무 feign client 호출
			if(!hubClient.isHubExists(reqHubId)) {
				log.error("Hub id " + reqHubId + " not found");
				return false;
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
			.assignmentCount(0)
			.build()
		);
		return true;
	}

	// 권한 - MASTER, 담당 HUB, 담당 DLV_AGENT
	@Transactional(readOnly = true)
	@Cacheable(value = "shippingAgentCache", key = "#shippingManagerId.toString()")
	public ShippingAgentResDto getShippingAgentById(UUID shippingManagerId) {

		ShippingAgent targetshippingAgent = shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId)
			.orElseThrow(() -> new ShippingAgentNotFoundException(shippingManagerId + ": 배송 관리자 ID가 존재하지 않습니다."));

		return ShippingAgentResDto.toResponse(targetshippingAgent);
	}

	// 권한 - MASTER, 담당 HUB, 담당 DLV_AGENT
	@Transactional(readOnly = true)
	@Cacheable(value = "shippingAgentSearchCache",
		key = "{#request.shippingManagerId, #request.hubId, #request.page, #request.size, #request.sortOption?.name(), #request.sortOrder?.name()}"
	)
	public PagedShippingAgentResDto searchShippingAgents(ShippingAgentSearchReqDto request) {
		Page<ShippingAgent> shippingAgentPage = shippingAgentRepository.findAll(request);

		Page<PagedShippingAgentItemResDto> shippingAgentItemDtoPage = shippingAgentPage
			.map(PagedShippingAgentItemResDto::toResponse);

		return PagedShippingAgentResDto.toResponseDto(shippingAgentItemDtoPage, request.getSortOption());
	}

	// 권한 - MASTER, 담당HUB
	// 1. 유저의 feign client 호출되어 넘겨받은 정보 변경
	@Caching(
		evict = {@CacheEvict(value = "shippingAgentSearchCache", allEntries = true)}
	)
	public boolean infoUpdateShippingAgent(
		ShippingAgentFeignClientPatchRequest request) {

		log.info("shippingManagerId : {}", request.getShippingManagerId());
		// shippingId 유효성 검사
		ShippingAgent target = shippingAgentRepository
			.findByShippingManagerIdAndDeletedAtIsNull(
				request.getShippingManagerId())
			.orElseThrow(() ->
				new ShippingAgentNotFoundException(
					"배송 관리자 ID:" + request.getShippingManagerId()  + "가 존재하지 않습니다."));

		// hubId 유효성 검사
		if(!hubClient.isHubExists(request.getHubId())) {
			log.info("Hub id " + request.getHubId() + " not found");
			return false;
		}

		target.preUpdate();
		shippingAgentRepository.save(target.toBuilder()
			.shippingManagerId(request.getShippingManagerId())
			.hubId(request.getHubId())
			.shippingManagerSlackId(request.getSlackId())
			.build())
			.toResponse();
		return true;
	}

	// 권한 - MASTER, 담당HUB
	// 2.배송 여부 확인 변경
	@Caching(
		put = {@CachePut(value = "shippingAgentCache", key = "#result.shippingManagerId.toString()")},
		evict = {@CacheEvict(value = "shippingAgentSearchCache", allEntries = true)
	})
	public ShippingAgentResDto transitUpdateShippingAgent(UUID shippingManagerId, Boolean isTransit, String userId) {
		log.info("isTransit: {}", isTransit);
		ShippingAgent target = shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId)
			.orElseThrow(() -> new ShippingAgentNotFoundException(shippingManagerId + ": 배송 관리자 ID가 존재하지 않습니다."));
		if(isTransit == target.getIsTransit()) {
			log.warn(isTransit + ": 배송여부가 동일하므로 수정되지 않았습니다.");
			throw new ShippingAgentNotUpdateException(isTransit + ": 배송여부가 동일하므로 수정되지 않았습니다.");
		}
		target.preUpdate();
		return shippingAgentRepository.save(target.toBuilder()
				.isTransit(isTransit)
				.createdBy(userId)
				.build())
			.toResponse();
	}

	// 권한 - MASTER, 담당HUB
	// 배송담당자 ID로 배송담당자 단일 조회 - User feign client 호출 요청 삭제
	@Caching(evict = {
		@CacheEvict(value = "shippingAgentCache", allEntries = true),
		@CacheEvict(value = "shippingAgentSearchCache", allEntries = true)
	})
	public boolean deleteShippingAgentForUser(UUID shippingManagerId) {
		ShippingAgent target = shippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId)
			.orElseThrow(() -> new ShippingAgentNotFoundException("해당 배송담당자 id 가 존재하지 않습니다."));

		if(target.getIsTransit()) {
			log.info("배송 가능 여부가 " + target.getIsTransit() + " 이므로 배송담당자 삭제가 불가능합니다.");
			return false;
		}
		shippingAgentRepository.save(target.softDelete());
		return true;
	}
}
