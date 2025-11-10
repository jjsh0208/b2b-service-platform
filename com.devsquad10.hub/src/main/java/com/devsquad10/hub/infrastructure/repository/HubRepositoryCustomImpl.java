package com.devsquad10.hub.infrastructure.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import com.devsquad10.hub.application.dto.enums.HubSortOption;
import com.devsquad10.hub.application.dto.req.HubSearchRequestDto;
import com.devsquad10.hub.domain.model.Hub;
import com.devsquad10.hub.domain.model.QHub;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HubRepositoryCustomImpl implements HubRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	QHub hub = QHub.hub;

	@Override
	public Page<Hub> findAll(HubSearchRequestDto request) {

		BooleanBuilder builder = new BooleanBuilder();

		if (request.getId() != null) {
			builder.and(hub.id.eq(request.getId()));
		}

		if (StringUtils.hasText(request.getName())) {
			builder.and(hub.name.containsIgnoreCase(request.getName()));
		}

		if (StringUtils.hasText(request.getAddress())) {
			builder.and(hub.address.containsIgnoreCase(request.getAddress()));
		}

		List<Hub> data = queryFactory.select(hub).from(hub)
			.where(builder)
			.offset((long)request.getPage() * request.getSize())
			.limit(request.getSize())
			.orderBy(getOrderSpecifier(request.getSortOption(), request.getSortOrder()))
			.fetch();

		Long total = queryFactory.select(hub.count())
			.from(hub)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(
			data,
			PageRequest.of(request.getPage(), request.getSize()),
			total != null ? total : 0L
		);
	}

	private OrderSpecifier<?> getOrderSpecifier(HubSortOption sortOption, Sort.Direction direction) {
		switch (sortOption) {
			case CREATED_AT:
				return direction == Sort.Direction.ASC ? hub.createdAt.asc() : hub.createdAt.desc();
			case UPDATED_AT:
				return direction == Sort.Direction.ASC ? hub.updatedAt.asc() : hub.updatedAt.desc();
			default:
				return hub.createdAt.asc();
		}
	}
}
