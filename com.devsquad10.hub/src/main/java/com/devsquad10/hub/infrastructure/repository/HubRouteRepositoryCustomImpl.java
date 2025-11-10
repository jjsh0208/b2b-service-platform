package com.devsquad10.hub.infrastructure.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import com.devsquad10.hub.application.dto.enums.HubRouteSortOption;
import com.devsquad10.hub.application.dto.req.HubRouteSearchRequestDto;
import com.devsquad10.hub.domain.model.HubRoute;
import com.devsquad10.hub.domain.model.QHubRoute;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HubRouteRepositoryCustomImpl implements HubRouteRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	QHubRoute hubRoute = QHubRoute.hubRoute;

	@Override
	public Page<HubRoute> findAll(HubRouteSearchRequestDto request) {

		BooleanBuilder builder = new BooleanBuilder();

		if (request.getId() != null) {
			builder.and(hubRoute.id.eq(request.getId()));
		}

		if (request.getDepartureHubId() != null) {
			builder.and(hubRoute.departureHub.id.eq(request.getDepartureHubId()));
		}

		if (request.getDestinationHubId() != null) {
			builder.and(hubRoute.destinationHub.id.eq(request.getDestinationHubId()));
		}

		if (StringUtils.hasText(request.getDepartureHubName())) {
			builder.and(hubRoute.departureHub.name.containsIgnoreCase(request.getDepartureHubName()));
		}

		if (StringUtils.hasText(request.getDestinationHubName())) {
			builder.and(hubRoute.destinationHub.name.containsIgnoreCase(request.getDestinationHubName()));
		}

		if (request.getMinDistance() != null) {
			builder.and(hubRoute.distance.goe(request.getMinDistance()));
		}

		if (request.getMaxDistance() != null) {
			builder.and(hubRoute.distance.loe(request.getMaxDistance()));
		}

		if (request.getMinDuration() != null) {
			builder.and(hubRoute.duration.goe(request.getMinDuration()));
		}

		if (request.getMaxDuration() != null) {
			builder.and(hubRoute.duration.loe(request.getMaxDuration()));
		}

		List<HubRoute> data = queryFactory.select(hubRoute).from(hubRoute)
			.where(builder)
			.offset((long)request.getPage() * request.getSize())
			.limit(request.getSize())
			.orderBy(getOrderSpecifier(request.getSortOption(), request.getSortOrder()))
			.fetch();

		Long total = queryFactory.select(hubRoute.count())
			.from(hubRoute)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(
			data,
			PageRequest.of(request.getPage(), request.getSize()),
			total != null ? total : 0L
		);
	}

	private OrderSpecifier<?> getOrderSpecifier(HubRouteSortOption sortOption, Sort.Direction direction) {
		switch (sortOption) {
			case CREATED_AT:
				return direction == Sort.Direction.ASC ? hubRoute.createdAt.asc() : hubRoute.createdAt.desc();
			case UPDATED_AT:
				return direction == Sort.Direction.ASC ? hubRoute.updatedAt.asc() : hubRoute.updatedAt.desc();
			case DISTANCE:
				return direction == Sort.Direction.ASC ? hubRoute.distance.asc() : hubRoute.distance.desc();
			case DURATION:
				return direction == Sort.Direction.ASC ? hubRoute.duration.asc() : hubRoute.duration.desc();
			default:
				return hubRoute.createdAt.asc();
		}
	}
}
