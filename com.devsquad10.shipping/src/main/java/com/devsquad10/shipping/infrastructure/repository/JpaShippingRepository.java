package com.devsquad10.shipping.infrastructure.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devsquad10.shipping.domain.model.Shipping;

import jakarta.persistence.LockModeType;

@Repository
public interface JpaShippingRepository extends JpaRepository<Shipping, UUID>, ShippingRepositoryCustom {
	Optional<Shipping> findByIdAndDeletedAtIsNull(UUID id);

	// 비관적 락(배타적 락) : 배송담당자 배정할 때, 배송에서 사용
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from Shipping s where s.id = :id and s.deletedAt is null")
	Optional<Shipping> findByIdWithPessimisticLock(@Param("id") UUID id);

	Optional<Shipping> findByOrderIdAndDeletedAtIsNull(UUID id);

	// @Query("SELECT s FROM Shipping s WHERE DATE(s.deadLine) = CURRENT_DATE")
	@Query("SELECT s FROM Shipping s WHERE s.deletedAt is NULL AND FUNCTION('DATE', s.deadLine) = CURRENT_DATE")
	List<Shipping> findShippingWithDeadlineToday();
}
