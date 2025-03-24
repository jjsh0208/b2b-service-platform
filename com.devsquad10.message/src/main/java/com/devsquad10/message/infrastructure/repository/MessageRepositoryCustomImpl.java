package com.devsquad10.message.infrastructure.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import com.devsquad10.message.application.dto.enums.MessageSortOption;
import com.devsquad10.message.application.dto.req.MessageSearchRequestDto;
import com.devsquad10.message.domain.model.Message;
import com.devsquad10.message.domain.model.QMessage;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageRepositoryCustomImpl implements MessageRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	QMessage message = QMessage.message1;

	@Override
	public Page<Message> findAll(MessageSearchRequestDto request) {
		BooleanBuilder builder = new BooleanBuilder();

		if (request.getId() != null) {
			builder.and(message.id.eq(request.getId()));
		}

		if (StringUtils.hasText(request.getMessage())) {
			builder.and(message.message.containsIgnoreCase(request.getMessage()));
		}

		List<Message> data = queryFactory.select(message).from(message)
			.where(builder)
			.offset((long)request.getPage() * request.getSize())
			.limit(request.getSize())
			.orderBy(getOrderSpecifier(request.getSortOption(), request.getSortOrder()))
			.fetch();

		Long total = queryFactory.select(message.count())
			.from(message)
			.where(builder)
			.fetchOne();

		return new PageImpl<>(
			data,
			PageRequest.of(request.getPage(), request.getSize()),
			total != null ? total : 0L
		);
	}

	private OrderSpecifier<?> getOrderSpecifier(MessageSortOption sortOption, Sort.Direction direction) {
		switch (sortOption) {
			case CREATED_AT:
				return direction == Sort.Direction.ASC ? message.createdAt.asc() : message.createdAt.desc();
			case UPDATED_AT:
				return direction == Sort.Direction.ASC ? message.updatedAt.asc() : message.updatedAt.desc();
			default:
				return message.createdAt.asc();
		}
	}
}
