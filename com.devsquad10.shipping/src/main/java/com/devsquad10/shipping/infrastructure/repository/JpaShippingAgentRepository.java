package com.devsquad10.shipping.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devsquad10.shipping.domain.model.ShippingAgent;

import jakarta.persistence.LockModeType;

@Repository
public interface JpaShippingAgentRepository extends JpaRepository<ShippingAgent, UUID>, ShippingAgentRepositoryCustom {
	// 순차적 순번 배정을 위한 최대값 추출
	@Query("SELECT MAX(a.shippingSequence) FROM ShippingAgent a")
	Optional<Integer> findMaxShippingSequence();

	// 배송담당자 사용자ID 조회
	Optional<ShippingAgent> findByShippingManagerIdAndDeletedAtIsNull(UUID shippingManagerId);

	// 비관적 락(배타적 락) : 배송담당자 배정할 때, 사용
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select sa from ShippingAgent sa where sa.id = :id and sa.deletedAt is null")
	ShippingAgent findByIdWithPessimisticLock(@Param("id") UUID id);

	List<ShippingAgent> findAllByDeletedAtIsNull();
}
