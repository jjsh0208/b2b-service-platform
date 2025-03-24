package com.devsquad10.shipping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devsquad10.shipping.application.dto.request.ShippingAgentSearchReqDto;
import com.devsquad10.shipping.domain.model.ShippingAgent;
import com.devsquad10.shipping.infrastructure.repository.JpaShippingAgentRepository;

import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ShippingAgentRepositoryImpl implements ShippingAgentRepository {

	private final JpaShippingAgentRepository jpaShippingAgentRepository;

	@Override
	public ShippingAgent save(ShippingAgent shippingAgent) {
		return jpaShippingAgentRepository.save(shippingAgent);
	}

	// 순차적 순번 배정을 위한 최대값 추출
	@Override
	@Query("SELECT MAX(a.shippingSequence) FROM ShippingAgent a")
	public Optional<Integer> findMaxShippingSequence() {
		return jpaShippingAgentRepository.findMaxShippingSequence();
	}

	// 배송담당자 사용자ID 조회
	@Override
	public Optional<ShippingAgent> findByShippingManagerIdAndDeletedAtIsNull(UUID shippingManagerId) {
		return jpaShippingAgentRepository.findByShippingManagerIdAndDeletedAtIsNull(shippingManagerId);
	}

	// 비관적 락(배타적 락) : 배송담당자 배정할 때, 사용
	@Override
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select sa from ShippingAgent sa where sa.id = :id and sa.deletedAt is null")
	public ShippingAgent findByIdWithPessimisticLock(@Param("id") UUID id) {
		return jpaShippingAgentRepository.findByIdWithPessimisticLock(id);
	}

	@Override
	public List<ShippingAgent> findAllByDeletedAtIsNull() {
		return jpaShippingAgentRepository.findAllByDeletedAtIsNull();
	}

	@Override
	public Page<ShippingAgent> findAll(@Param("request") ShippingAgentSearchReqDto request) {
		return jpaShippingAgentRepository.findAll(request);
	}
}
