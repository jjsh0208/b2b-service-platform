package com.devsquad10.product.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devsquad10.product.domain.model.Product;
import com.devsquad10.product.domain.repository.ProductRepository;

import jakarta.persistence.LockModeType;

@Repository
public interface JpaProductRepository
	extends JpaRepository<Product, UUID>, QuerydslPredicateExecutor<Product>, ProductRepository {

	@Override
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Product p WHERE p.id = :productId AND p.deletedAt IS NULL")
	Optional<Product> findByIdWithLock(@Param("productId") UUID productId);

}
