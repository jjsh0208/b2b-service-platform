package com.devsquad10.shipping.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.devsquad10.shipping.domain.model.QShippingHistory;
import com.devsquad10.shipping.domain.model.ShippingHistory;
import com.devsquad10.shipping.domain.repository.ShippingHistoryRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;

@Repository
public interface JpaShippingHistoryRepository
	extends JpaRepository<ShippingHistory, UUID>,
	QuerydslPredicateExecutor<ShippingHistory>,
	ShippingHistoryRepository {

	default Page<ShippingHistory> findAll(String query, String category, int page, int size, String sortBy, String order) {
		QShippingHistory shippingHistory = QShippingHistory.shippingHistory;

		// 검색 조건 생성
		BooleanBuilder builder = buildSearchConditions(query, category, shippingHistory);

		Sort sort = getSortOrder(sortBy, order);

		PageRequest pageRequest = PageRequest.of(page, size, sort);

		return findAll(builder, pageRequest);
	}


	private BooleanBuilder buildSearchConditions(String query, String category, QShippingHistory qShippingHistory) {

		BooleanBuilder builder = new BooleanBuilder();

		builder.and(qShippingHistory.deletedBy.isNull());

		if (query == null || query.isEmpty()) {
			return builder;
		}

		if (category == null || category.isEmpty()) {
			// 카테고리 지정이 없으면 모든 필드에서 검색
			builder.or(parseUUID(query,qShippingHistory.shipping.id));
			builder.or(parseUUID(query,qShippingHistory.shippingManagerId));
			builder.or(qShippingHistory.historyStatus.stringValue().containsIgnoreCase(query));
		} else {
			switch (category) {
				case "shippingId":
					builder.or(parseUUID(query,qShippingHistory.shipping.id));
					break;
				case "shippingManagerId":
					builder.or(parseUUID(query,qShippingHistory.shippingManagerId));
					break;
				case "historyStatus":
					builder.or(qShippingHistory.historyStatus.stringValue().containsIgnoreCase(query));
					break;
				default:
					builder.or(parseUUID(query,qShippingHistory.shipping.id));
					builder.or(parseUUID(query,qShippingHistory.shippingManagerId));
					builder.or(qShippingHistory.historyStatus.stringValue().containsIgnoreCase(query));
					break;
			}
		}
		return builder;
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
