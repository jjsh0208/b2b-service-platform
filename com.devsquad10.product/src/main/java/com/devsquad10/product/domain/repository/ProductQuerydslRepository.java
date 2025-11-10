package com.devsquad10.product.domain.repository;

import org.springframework.data.domain.Page;

import com.devsquad10.product.domain.model.Product;

public interface ProductQuerydslRepository {
	Page<Product> findAll(String q, String category, int page, int size, String sort, String order);
}
