package com.devsquad10.product.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.devsquad10.product.domain.model.Product;
import com.devsquad10.product.domain.model.QProduct;
import com.devsquad10.product.domain.repository.ProductQuerydslRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;

public interface ProductQuerydslRepositoryCustom extends
	JpaRepository<Product, UUID>, QuerydslPredicateExecutor<Product>, ProductQuerydslRepository {

	default Page<Product> findAll(String query, String category, int page, int size, String sortBy,
		String order) {

		size = validateSize(size);

		QProduct product = QProduct.product;

		// 검색 조건 생성
		BooleanBuilder builder = buildSearchConditions(query, category, product);

		Sort sort = getSortOrder(sortBy, order);

		PageRequest pageRequest = PageRequest.of(page, size, sort);

		return findAll(builder, pageRequest);

	}

	private BooleanBuilder buildSearchConditions(String query, String category, QProduct qProduct) {

		BooleanBuilder builder = new BooleanBuilder();

		builder.and(qProduct.deletedAt.isNull());

		if (query == null || query.isEmpty()) {
			return builder;
		}

		if (category == null || category.isEmpty()) {
			// 카테고리 지정이 없으면 모든 필드에서 검색
			builder.or(qProduct.name.containsIgnoreCase(query));
			builder.or(parseUUID(query, qProduct.supplierId));
			builder.or(parseUUID(query, qProduct.hubId));
		} else {
			switch (category) {
				case "name":
					builder.or(qProduct.name.containsIgnoreCase(query));
					break;
				case "supplierId":
					builder.or(parseUUID(query, qProduct.supplierId));
					break;
				case "hubId":
					builder.or(parseUUID(query, qProduct.hubId));
					break;
				default:
					builder.or(qProduct.name.containsIgnoreCase(query));
					builder.or(parseUUID(query, qProduct.supplierId));
					builder.or(parseUUID(query, qProduct.hubId));
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
