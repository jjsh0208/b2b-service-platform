package com.devsquad10.shipping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

import com.devsquad10.shipping.application.dto.request.ShippingAgentSearchReqDto;
import com.devsquad10.shipping.domain.model.ShippingAgent;

public interface ShippingAgentRepository {
	ShippingAgent save(ShippingAgent shippingAgent);

	// 순차적 순번 배정을 위한 최대값 추출
	Optional<Integer> findMaxShippingSequence();

	// 배송담당자 사용자ID 조회
	Optional<ShippingAgent> findByShippingManagerIdAndDeletedAtIsNull(UUID shippingManagerId);

	// 비관적 락(배타적 락) : 배송담당자 배정할 때, 사용
	ShippingAgent findByIdWithPessimisticLock(@Param("id") UUID id);

	List<ShippingAgent> findAllByDeletedAtIsNull();

	Page<ShippingAgent> findAll(@Param("request") ShippingAgentSearchReqDto request);
}
