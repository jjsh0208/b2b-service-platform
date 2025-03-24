package com.devsquad10.message.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.devsquad10.message.application.dto.req.MessageSearchRequestDto;
import com.devsquad10.message.domain.model.Message;

public interface MessageRepository {
	Message save(Message message);

	Optional<Message> findById(UUID id);

	Page<Message> findAll(MessageSearchRequestDto request);
}
