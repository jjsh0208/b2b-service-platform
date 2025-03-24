package com.devsquad10.order.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.devsquad10.order.domain.model.Order;

public interface OrderRepository {

	Optional<Order> findByIdAndDeletedAtIsNull(UUID id);

	Order save(Order order);

	// shippingId로 deletedAt이 null인 Order를 찾는 메서드
	Optional<Order> findByShippingIdAndDeletedAtIsNull(UUID shippingId);

}
