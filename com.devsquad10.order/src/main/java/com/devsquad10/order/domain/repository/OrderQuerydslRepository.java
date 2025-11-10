package com.devsquad10.order.domain.repository;

import org.springframework.data.domain.Page;

import com.devsquad10.order.domain.model.Order;

public interface OrderQuerydslRepository {

	Page<Order> findAll(String q, String category, int page, int size, String sort, String order);
}
