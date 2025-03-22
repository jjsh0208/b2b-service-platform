package com.devsquad10.message.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import com.devsquad10.message.application.dto.req.MessageSearchRequestDto;
import com.devsquad10.message.domain.model.Message;
import com.devsquad10.message.infrastructure.repository.JpaMessageRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {
	private final JpaMessageRepository jpaMessageRepository;

	@Override
	public Message save(Message message) {
		return jpaMessageRepository.save(message);
	}

	@Override
	public Optional<Message> findById(UUID id) {
		return jpaMessageRepository.findById(id);
	}

	@Override
	public Page<Message> findAll(MessageSearchRequestDto request) {
		return jpaMessageRepository.findAll(request);
	}

}
