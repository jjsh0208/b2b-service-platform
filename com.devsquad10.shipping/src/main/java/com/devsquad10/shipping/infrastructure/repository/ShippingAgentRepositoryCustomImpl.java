package com.devsquad10.shipping.infrastructure.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.devsquad10.shipping.application.dto.enums.ShippingAgentSortOption;
import com.devsquad10.shipping.application.dto.request.ShippingAgentSearchReqDto;
import com.devsquad10.shipping.domain.model.QShippingAgent;
import com.devsquad10.shipping.domain.model.ShippingAgent;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShippingAgentRepositoryCustomImpl implements ShippingAgentRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	QShippingAgent shippingAgent = QShippingAgent.shippingAgent;

	@Override
	public Page<ShippingAgent> findAll(ShippingAgentSearchReqDto request) {
		BooleanBuilder	builder = new BooleanBuilder();

		if(request.getShippingManagerId() != null) {
			builder.and(shippingAgent.shippingManagerId.eq(request.getShippingManagerId()));
		}

		if(request.getHubId() != null) {
			builder.and(shippingAgent.hubId.eq(request.getHubId()));
		}

		List<ShippingAgent> shippingAgents = queryFactory.select(shippingAgent).from(shippingAgent)
			.where(builder)
			.offset((long)request.getPage() * request.getSize())
			.limit(request.getSize())
			.orderBy(getOrderSpecifier(request.getSortOption(), request.getSortOrder()))
			.fetch();

		Long total = queryFactory.select(shippingAgent.count())
			.from(shippingAgent)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(
			shippingAgents,
			PageRequest.of(request.getPage(), request.getSize()),
			total != null ? total : 0L
		);
	}

	private OrderSpecifier<?> getOrderSpecifier(ShippingAgentSortOption sortOption, Sort.Direction direction) {
		switch (sortOption) {
			case CREATED_AT:
				return direction == Sort.Direction.ASC ? shippingAgent.createdAt.asc() : shippingAgent.createdAt.desc();
			case UPDATED_AT:
				return direction == Sort.Direction.ASC ? shippingAgent.updatedAt.asc() : shippingAgent.updatedAt.desc();
			default:
				return shippingAgent.createdAt.asc();
		}
	}
}
