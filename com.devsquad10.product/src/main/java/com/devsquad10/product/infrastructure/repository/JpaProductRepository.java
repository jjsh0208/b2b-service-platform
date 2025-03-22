package com.devsquad10.product.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.devsquad10.product.domain.model.Product;
import com.devsquad10.product.domain.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Repository
public interface JpaProductRepository
	extends JpaRepository<Product, UUID>, QuerydslPredicateExecutor<Product>, ProductRepository {

	@Modifying
	@Transactional
	@Query("UPDATE Product p SET p.quantity = p.quantity - :orderQuantity " +
		"WHERE p.id = :productId AND p.quantity >= :orderQuantity")
	int decreaseStock(@Param("productId") UUID productId, @Param("orderQuantity") int orderQuantity);

}
