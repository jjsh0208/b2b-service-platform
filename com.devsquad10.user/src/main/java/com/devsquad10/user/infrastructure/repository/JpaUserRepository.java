package com.devsquad10.user.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.devsquad10.user.domain.model.QUser;
import com.devsquad10.user.domain.model.User;
import com.devsquad10.user.domain.model.UserRoleEnum;
import com.devsquad10.user.domain.repository.UserRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public interface JpaUserRepository
	extends JpaRepository<User, UUID>, QuerydslPredicateExecutor<User>, UserRepository {

	default Page<User> findByUsernameContainingAndRole(String query, UserRoleEnum role, int page, int size,
		String sortBy, String order,
		JPAQueryFactory queryFactory) {
		QUser user = QUser.user;

		// 🔹 검색 조건 설정
		BooleanBuilder builder = buildSearchConditions(query, role, user);

		// 🔹 정렬 방식 설정
		Sort sort = getSortOrder(sortBy, order);

		// 🔹 페이징 요청 객체 생성
		PageRequest pageRequest = PageRequest.of(page, size, sort);

		// 🔹 QueryDSL을 활용한 검색 및 페이징 처리
		List<User> users = queryFactory
			.selectFrom(user)
			.where(builder)
			.offset(pageRequest.getOffset())
			.limit(pageRequest.getPageSize())
			.fetch();

		long total = queryFactory
			.selectFrom(user)
			.where(builder)
			.fetchCount();

		return PageableExecutionUtils.getPage(users, pageRequest, () -> total);
	}

	private BooleanBuilder buildSearchConditions(String query, UserRoleEnum role, QUser user) {
		BooleanBuilder builder = new BooleanBuilder();

		builder.and(user.deletedBy.isNull()); // 삭제되지 않은 사용자만 검색

		if (role != null) {
			builder.and(user.role.eq(role));
		}

		if (query != null && !query.isEmpty()) {
			builder.or(user.username.containsIgnoreCase(query));
			builder.or(user.email.containsIgnoreCase(query));
			builder.or(user.slackId.containsIgnoreCase(query));
		}

		return builder;
	}

	/**
	 * 정렬 방식 설정
	 */
	private Sort getSortOrder(String sortBy, String order) {
		if (!isValidSortBy(sortBy)) {
			throw new IllegalArgumentException("SortBy 는 'createdAt', 'updatedAt', 'deletedAt' 값만 허용합니다.");
		}

		Sort sort = Sort.by(Sort.Order.by(sortBy));

		return getSortDirection(sort, order);
	}

	private boolean isValidSortBy(String sortBy) {
		return "createdAt".equals(sortBy) || "updatedAt".equals(sortBy) || "deletedAt".equals(sortBy);
	}

	private Sort getSortDirection(Sort sort, String order) {
		return "desc".equalsIgnoreCase(order) ? sort.descending() : sort.ascending();
	}
}
