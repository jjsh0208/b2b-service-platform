package com.devsquad10.shipping.domain.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devsquad10.shipping.application.dto.request.ShippingSearchReqDto;
import com.devsquad10.shipping.domain.model.Shipping;
import com.devsquad10.shipping.infrastructure.repository.JpaShippingRepository;

import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ShippingRepositoryImpl implements ShippingRepository{

	private final JpaShippingRepository jpaShippingRepository;

	@Override
	public Shipping save(Shipping shipping) {
		return jpaShippingRepository.save(shipping);
	}

	@Override
	public Optional<Shipping> findByIdAndDeletedAtIsNull(UUID id) {
		return jpaShippingRepository.findByIdAndDeletedAtIsNull(id);
	}

	// 비관적 락(배타적 락) : 배송담당자 배정할 때, 배송에서 사용
	@Override
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from Shipping s where s.id = :id and s.deletedAt is null")
	public Optional<Shipping> findByIdWithPessimisticLock(@Param("id") UUID id) {
		return jpaShippingRepository.findByIdWithPessimisticLock(id);
	}

	@Override
	public Page<Shipping> findAll(@Param("request") ShippingSearchReqDto request) {
		return jpaShippingRepository.findAll(request);
	}

	@Override
	public Optional<Shipping> findByOrderIdAndDeletedAtIsNull(UUID orderId) {
		return jpaShippingRepository.findByOrderIdAndDeletedAtIsNull(orderId);
	}

	@Override
	@Query("SELECT s FROM Shipping s WHERE s.deadLine = CURRENT_DATE")
	public List<Shipping> findShippingWithDeadlineToday(Date deadLine) {
		return jpaShippingRepository.findShippingWithDeadlineToday(deadLine);
	}
}
