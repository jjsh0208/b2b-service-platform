package com.devsquad10.shipping.infrastructure.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;

import com.devsquad10.shipping.application.dto.enums.ShippingSortOption;
import com.devsquad10.shipping.application.dto.request.ShippingSearchReqDto;
import com.devsquad10.shipping.domain.model.QShipping;
import com.devsquad10.shipping.domain.model.Shipping;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShippingRepositoryCustomImpl implements ShippingRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	QShipping shipping = QShipping.shipping;

	@Override
	public Page<Shipping> findAll(@Param("request") ShippingSearchReqDto request) {
		BooleanBuilder builder = new BooleanBuilder();

		if (request.getId() != null) {
			builder.and(shipping.id.eq(request.getId()));
		}

		if (request.getDepartureHubId() != null) {
			builder.and(shipping.departureHubId.eq(request.getDepartureHubId()));
		}

		if (request.getDestinationHubId() != null) {
			builder.and(shipping.destinationHubId.eq(request.getDestinationHubId()));
		}

		if (request.getStatus() != null) {
			builder.and(shipping.status.eq(request.getStatus()));
		}

		if (request.getCompanyShippingManagerId() != null) {
			builder.and(shipping.companyShippingManagerId.eq(request.getCompanyShippingManagerId()));
		}

		List<Shipping> shippingList = queryFactory.select(shipping).from(shipping)
			.where(builder)
			.offset((long)request.getSize() * request.getPage())
			.limit(request.getSize())
			.orderBy(getOrderSpecifier(request.getSortOption(), request.getSortOrder()))
			.fetch();

		Long total = queryFactory.select(shipping.count())
			.from(shipping)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(
			shippingList,
			PageRequest.of(request.getPage(), request.getSize()),
			total != null ? total : 0L
		);
	}

	private OrderSpecifier<?> getOrderSpecifier(ShippingSortOption sortOption, Sort.Direction direction) {
		switch (sortOption) {
			case CREATED_AT -> {
				return direction == Sort.Direction.ASC ? shipping.createdAt.asc() : shipping.createdAt.desc();
			}
			case UPDATED_AT -> {
				return direction == Sort.Direction.ASC ? shipping.updatedAt.asc() : shipping.updatedAt.desc();
			}
			default -> {
				return direction == Sort.Direction.ASC ? shipping.createdAt.desc() : shipping.createdAt.asc();
			}
		}
	}
}
