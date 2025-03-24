package com.devsquad10.message.infrastructure.repository;

import org.springframework.data.domain.Page;

import com.devsquad10.message.application.dto.req.MessageSearchRequestDto;
import com.devsquad10.message.domain.model.Message;

public interface MessageRepositoryCustom {
	Page<Message> findAll(MessageSearchRequestDto request);
}
