package com.devsquad10.company.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.devsquad10.company.domain.model.Company;
import com.devsquad10.company.domain.model.QCompany;
import com.devsquad10.company.domain.repository.CompanyQuerydslRepository;
import com.querydsl.core.BooleanBuilder;

@Repository
public interface CompanyQuerydslRepositoryCustom
	extends JpaRepository<Company, UUID>, QuerydslPredicateExecutor<Company>, CompanyQuerydslRepository {
	default Page<Company> findAll(String query, String category, int page, int size, String sortBy,
		String order) {

		size = validateSize(size);

		QCompany company = QCompany.company;

		// 검색 조건 생성
		BooleanBuilder builder = buildSearchConditions(query, category, company);

		Sort sort = getSortOrder(sortBy, order);

		PageRequest pageRequest = PageRequest.of(page, size, sort);

		return findAll(builder, pageRequest);

	}

	private BooleanBuilder buildSearchConditions(String query, String category, QCompany qCompany) {

		BooleanBuilder builder = new BooleanBuilder();

		builder.and(qCompany.deletedBy.isNull());

		if (query == null || query.isEmpty()) {
			return builder;
		}

		if (category == null || category.isEmpty()) {
			// 카테고리 지정이 없으면 모든 필드에서 검색
			builder.or(qCompany.name.containsIgnoreCase(query));
			builder.or(qCompany.address.containsIgnoreCase(query));
			builder.or(qCompany.type.stringValue().containsIgnoreCase(query));
		} else {
			switch (category) {
				case "name":
					builder.or(qCompany.name.containsIgnoreCase(query));
					break;
				case "address":
					builder.or(qCompany.address.containsIgnoreCase(query));
					break;
				case "type":
					builder.or(qCompany.type.stringValue().containsIgnoreCase(query));
					break;
				default:
					builder.or(qCompany.name.containsIgnoreCase(query));
					builder.or(qCompany.address.containsIgnoreCase(query));
					builder.or(qCompany.type.stringValue().containsIgnoreCase(query));
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
