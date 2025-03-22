package com.devsquad10.shipping.application.service;

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
import com.devsquad10.shipping.application.dto.request.ShippingUpdateReqDto;
import com.devsquad10.shipping.application.dto.response.ShippingResDto;
import com.devsquad10.shipping.application.exception.shipping.ShippingNotFoundException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentAlreadyAllocatedException;
import com.devsquad10.shipping.application.exception.shippingAgent.ShippingAgentNotAllocatedException;
import com.devsquad10.shipping.application.service.allocation.ShippingAgentAllocation;
import com.devsquad10.shipping.domain.model.Shipping;
import com.devsquad10.shipping.domain.model.ShippingHistory;
import com.devsquad10.shipping.domain.repository.ShippingHistoryRepository;
import com.devsquad10.shipping.domain.repository.ShippingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShippingService {

	private final ShippingRepository shippingRepository;
	private final ShippingHistoryRepository shippingHistoryRepository;
	private final ShippingAgentAllocation shippingAgentAllocation;

	// TODO: 권한 확인 - MASTER, 담당 HUB, DVL_AGENT
	// 변경1. 배송 정보(수령인, 수령인 번호) update
	@CachePut(cacheNames = "shippingCache", key = "#result.id", condition = "#id != null")
	@Caching(evict = {
		@CacheEvict(cacheNames = "shippingSearchCache", allEntries = true)
	})
	public ShippingResDto updateShipping(UUID id, ShippingUpdateReqDto shippingUpdateReqDto) {
		Shipping shipping = shippingRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ShippingNotFoundException("ID " + id + "에 해당하는 배송 데이터를 찾을 수 없습니다."));

		shipping.preUpdate();
		return shippingRepository.save(shipping.toBuilder()
			.recipientName((shippingUpdateReqDto.getRecipientName() == null) ? shipping.getRecipientName() : shippingUpdateReqDto.getRecipientName())
			.recipientSlackId((shippingUpdateReqDto.getRecipientPhone() == null) ? shipping.getRecipientSlackId() : shippingUpdateReqDto.getRecipientPhone())
			.build()).toResponseDto();
	}
	// 변경2. 배송 추적에 따른 현재상태(HUB_ARV) update
	@CachePut(cacheNames = "shippingCache", key = "#result.id", condition = "#id != null")
	@Caching(evict = {
		@CacheEvict(cacheNames = "shippingSearchCache", allEntries = true)
	})
	public ShippingResDto statusUpdateShipping(UUID id, ShippingUpdateReqDto shippingUpdateReqDto) {
		Shipping shipping = shippingRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ShippingNotFoundException("ID " + id + "에 해당하는 배송 데이터를 찾을 수 없습니다."));

		shipping.preUpdate();
		return shippingRepository.save(shipping.toBuilder()
			.status(shippingUpdateReqDto.getStatus())
			.build()).toResponseDto();
	}
	// 변경3. 주문정보(배송 주소지, 요청사항) update
	@CachePut(cacheNames = "shippingCache", key = "#result.id", condition = "#id != null")
	@Caching(evict = {
		@CacheEvict(cacheNames = "shippingSearchCache",
			allEntries = true,
			condition = "@cacheManager.getCache('shippingSearchCache') != null"
		)
	})
	public ShippingResDto infoUpdateShipping(UUID id, ShippingUpdateReqDto shippingUpdateReqDto) {
		Shipping shipping = shippingRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ShippingNotFoundException("ID " + id + "에 해당하는 배송 데이터를 찾을 수 없습니다."));

		shipping.preUpdate();
		return shippingRepository.save(shipping.toBuilder()
			.address((shippingUpdateReqDto.getAddress() == null) ? shipping.getAddress() : shippingUpdateReqDto.getAddress())
			.requestDetails(shippingUpdateReqDto.getRequestDetails())
			.build()).toResponseDto();
	}

	// 배정 로직 구현 후, 업체배송담당자 ID update
	@CachePut(cacheNames = "shippingCache", key = "#result.id", condition = "#id != null")
	@Caching(evict = {
		@CacheEvict(cacheNames = "shippingSearchCache", allEntries = true)
	})
	//TODO: 배송 담당자 배정 처리(주문 생성 전송시간 기준으로 허브간 이동이 시작되었다고 가정)
	// 전송시간+예상소요시간 기준
	// 배송 경로기록 마지막 순번의 현재상태가 "목적지 허브 도착:HUB_ARV"일 때만 배정 가능
	// 배송 진행여부 확인해서 "대기 중:False"일 때만 라운드 로빈 배정
	public ShippingResDto allocationShipping(UUID id) {
		// 동시성 처리로 인해 비관적 락 적용하여 동시성 제어
		Shipping shipping = shippingRepository.findByIdWithPessimisticLock(id)
			.orElseThrow(() -> new ShippingNotFoundException("ID " + id + "에 해당하는 배송 데이터를 찾을 수 없습니다."));

		// 배정 횟수 컬럼 추가하여 배정 시 횟수 업데이트 구현 -> 최소 배정 건수인 배송담당자 선택
		// 배송담당자 ID가 이미 배정된 경우 처리
		if (shipping.getCompanyShippingManagerId() != null) {
			throw new ShippingAgentAlreadyAllocatedException("업체 배송담당자가 이미 배정되어 담당자배정 불가합니다.");
		}
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

	// TODO: 권한 확인 - ALL + 담당 HUB, DVL_AGENT
	// TODO: 캐싱 처리 안됨 - postgres 데이터 있음
	@Cacheable(cacheNames = "shippingCache", key = "#id", condition = "#id != null")
	@Transactional(readOnly = true)
	public ShippingResDto getShippingById(UUID id) {
		return shippingRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ShippingNotFoundException(id + " 해당하는 배송 ID가 존재하지 않습니다."))
			.toResponseDto();
	}

	// TODO: 권한 확인 - ALL + 담당 HUB, DVL_AGENT
	// TODO: query, category != null 인 경우, queryDSL 적용 안됨
	@Cacheable(cacheNames = "shippingSearchCache", key = "#query +'=' + #category")
	@Transactional(readOnly = true)
	public Page<ShippingResDto> searchShipping(String query, String category, int page, int size, String sort, String order) {
		Page<Shipping> shippingPage = shippingRepository.findAll(query, category, page, size, sort, order);

		log.info("query {}", query);
		log.info("category {}", category);
		return shippingPage.map(Shipping::toResponseDto);
	}

	// TODO: 권한 확인 - MASTER, 담당 HUB
	//TODO: 1) condition="#id != null"인 경우, 개별 캐싱(shippingCache) 삭제 안됨
// 			2) condition 없는 경우, 캐싱 삭제 자체가 안됨 & postgres 의 데이터는 삭제됨
	@Caching(evict = {
		@CacheEvict(cacheNames = "shippingCache", key = "#id"),
		@CacheEvict(cacheNames = "shippingSearchCache",
			allEntries = true,
			condition = "@cacheManager.getCache('shippingSearchCache') != null")
	})

	public void deleteShipping(UUID id) {
		Shipping shipping = shippingRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ShippingNotFoundException(id + " 해당하는 배송 ID가 존재하지 않습니다."));

		// 배송 삭제 처리
		shippingRepository.save(shipping.softDelete());

		// 배송 ID로 배송경로기록 List 추출
		List<ShippingHistory> historyList = shippingHistoryRepository.findByShippingIdAndDeletedAtIsNull(id);

		// 배송 삭제 될 때, 배송 경로기록도 삭제 처리
		if(!historyList.isEmpty()) {
			List<UUID> historyIdList = historyList.stream()
				.map(ShippingHistory::getId)
				.toList();
			for(UUID historyId : historyIdList) {
				ShippingHistory history = shippingHistoryRepository.findByIdAndDeletedAtIsNull(historyId);
				shippingHistoryRepository.save(history.softDelete());
			}
		}
	}
}
