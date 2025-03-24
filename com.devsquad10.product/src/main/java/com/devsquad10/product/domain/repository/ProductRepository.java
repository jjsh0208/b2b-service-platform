package com.devsquad10.product.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.devsquad10.product.domain.model.Product;

public interface ProductRepository {

	Optional<Product> findByIdAndDeletedAtIsNull(UUID id);

	Product save(Product product);

	Optional<Product> findByIdWithLock(UUID productId);
}
