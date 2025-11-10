package com.devsquad10.shipping.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.devsquad10.shipping.domain.model.ShippingHistory;

public interface ShippingHistoryRepository {

	ShippingHistory save(ShippingHistory shippingHistory);

	// Page<ShippingHistory> findAll(String q, String category, int page, int size, String sort, String order);

	List<ShippingHistory> findByShippingIdAndDeletedAtIsNull(UUID id);

	ShippingHistory findByIdAndDeletedAtIsNull(UUID historyId);
}
