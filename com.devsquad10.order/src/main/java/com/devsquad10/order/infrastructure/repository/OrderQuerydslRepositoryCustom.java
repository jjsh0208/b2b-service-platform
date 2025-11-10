package com.devsquad10.order.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.devsquad10.order.domain.model.Order;
import com.devsquad10.order.domain.model.QOrder;
import com.devsquad10.order.domain.repository.OrderQuerydslRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;

public interface OrderQuerydslRepositoryCustom
	extends JpaRepository<Order, UUID>, QuerydslPredicateExecutor<Order>, OrderQuerydslRepository {

	default Page<Order> findAll(String query, String category, int page, int size, String sortBy,
		String order) {

		size = validateSize(size);

		QOrder qOrder = QOrder.order;

		// 검색 조건 생성
		BooleanBuilder builder = buildSearchConditions(query, category, qOrder);

		Sort sort = getSortOrder(sortBy, order);

		PageRequest pageRequest = PageRequest.of(page, size, sort);

		return findAll(builder, pageRequest);

	}

	private BooleanBuilder buildSearchConditions(String query, String category, QOrder qOrder) {

		BooleanBuilder builder = new BooleanBuilder();

		builder.and(qOrder.deletedAt.isNull());

		if (query == null || query.isEmpty()) {
			return builder;
		}

		if (category == null || category.isEmpty()) {
			// 카테고리 지정이 없으면 모든 필드에서 검색
			builder.or(parseUUID(query, qOrder.supplierId));
			builder.or(parseUUID(query, qOrder.recipientsId));
			builder.or(parseUUID(query, qOrder.productId));
			builder.or(parseUUID(query, qOrder.shippingId));
		} else {
			switch (category) {
				case "supplier":
					builder.or(parseUUID(query, qOrder.supplierId));
					break;
				case "recipients":
					builder.or(parseUUID(query, qOrder.recipientsId));
					break;
				case "product":
					builder.or(parseUUID(query, qOrder.productId));
					break;
				case "shipping":
					builder.or(parseUUID(query, qOrder.shippingId));
					break;
				default:
					builder.or(parseUUID(query, qOrder.supplierId));
					builder.or(parseUUID(query, qOrder.recipientsId));
					builder.or(parseUUID(query, qOrder.productId));
					builder.or(parseUUID(query, qOrder.shippingId));
					break;
			}
		}

		return builder;
	}

	private int validateSize(int size) {
		if (size != 10 && size != 30 && size != 50) {
			size = 10;
		}
		return size;
	}

	private BooleanBuilder parseUUID(String query, ComparableExpressionBase<UUID> uuidField) {
		try {
			UUID uuidQuery = UUID.fromString(query);
			return new BooleanBuilder(uuidField.eq(uuidQuery));
		} catch (IllegalArgumentException e) {
			return new BooleanBuilder(); // 잘못된 uuid이면 빈 조건 검색 반환 (검색 무시)
		}
	}

	private Sort getSortOrder(String sortBy, String order) {

		if (!isValidSortBy(sortBy)) {
			throw new IllegalArgumentException("SortBy 는 'createdAt', 'updatedAt', 'deletedAt' 값만 허용합니다.");
		}

		Sort sort = Sort.by(Sort.Order.by(sortBy));

		sort = getSortDirection(sort, order);

		return sort;
	}

	private boolean isValidSortBy(String sortBy) {

		return "createdAt".equals(sortBy) || "updatedAt".equals(sortBy) || "deletedAt".equals(sortBy);
	}

	private Sort getSortDirection(Sort sort, String order) {
		return "desc".equals(order) ? sort.descending() : sort.ascending();
	}
}
