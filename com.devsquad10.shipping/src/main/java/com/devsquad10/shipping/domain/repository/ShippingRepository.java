package com.devsquad10.shipping.domain.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.devsquad10.shipping.application.dto.request.ShippingSearchReqDto;
import com.devsquad10.shipping.domain.model.Shipping;

import jakarta.persistence.LockModeType;

public interface ShippingRepository {

	Shipping save(Shipping shipping);

	Optional<Shipping> findByIdAndDeletedAtIsNull(UUID id);

	// 비관적 락(배타적 락) : 배송담당자 배정할 때, 배송에서 사용
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from Shipping s where s.id = :id and s.deletedAt is null")
	Optional<Shipping> findByIdWithPessimisticLock(@Param("id") UUID id);

	Page<Shipping> findAll(@Param("request") ShippingSearchReqDto request);

	// 주문Id로 배송 조회
	Optional<Shipping> findByOrderIdAndDeletedAtIsNull(UUID orderId);

	// 납품기한이 오늘 날짜인 배송 전체 조회
	@Query("SELECT s FROM Shipping s WHERE s.deletedAt is NULL "
		+ "AND s.companyShippingManagerId is Not Null "
		+ "AND FUNCTION('DATE', s.deadLine) = CURRENT_DATE")
	List<Shipping> findShippingWithDeadlineToday();
}
