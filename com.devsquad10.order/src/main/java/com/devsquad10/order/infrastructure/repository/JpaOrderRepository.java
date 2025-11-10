package com.devsquad10.order.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.devsquad10.order.domain.model.Order;
import com.devsquad10.order.domain.repository.OrderRepository;

public interface JpaOrderRepository
	extends JpaRepository<Order, UUID>, QuerydslPredicateExecutor<Order>, OrderRepository {

}
