package com.devsquad10.message.application.service;

import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.devsquad10.message.application.dto.req.MessageCreateRequestDto;
import com.devsquad10.message.application.dto.req.MessageSearchRequestDto;
import com.devsquad10.message.application.dto.req.MessageUpdateRequestDto;
import com.devsquad10.message.application.dto.res.MessageCreateResponseDto;
import com.devsquad10.message.application.dto.res.MessageGetOneResponseDto;
import com.devsquad10.message.application.dto.res.MessageUpdateResponseDto;
import com.devsquad10.message.application.dto.res.PagedMessageItemResponseDto;
import com.devsquad10.message.application.dto.res.PagedMessageResponseDto;
import com.devsquad10.message.application.exception.MessageNotFoundException;
import com.devsquad10.message.domain.model.Message;
import com.devsquad10.message.domain.repository.MessageRepository;
import com.devsquad10.message.infrastructure.client.GeminiClient;
import com.devsquad10.message.infrastructure.client.SlackClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {
	private final MessageRepository messageRepository;
	private final GeminiClient geminiClient;
	private final SlackClient slackClient;

	@Caching(evict = {
		@CacheEvict(value = "messageSearchCache", allEntries = true)
	})
	public MessageCreateResponseDto createMessage(MessageCreateRequestDto request) {
		Message message = Message.builder()
			.message(request.getName())
			.recipientId(request.getRecipientId())
			.build();

		Message savedMessage = messageRepository.save(message);

		return MessageCreateResponseDto.toResponseDto(savedMessage);
	}

	@Cacheable(value = "messageCache", key = "#id.toString()")
	public MessageGetOneResponseDto getOneMessage(UUID id) {
		Message message = messageRepository.findById(id)
			.orElseThrow(() -> new MessageNotFoundException("Message not found with id: " + id.toString()));

		return MessageGetOneResponseDto.toResponseDto(message);
	}

	@Caching(
		put = {@CachePut(value = "messageCache", key = "#id.toString()")},
		evict = {@CacheEvict(value = "messageSearchCache", allEntries = true)}
	)
	public MessageUpdateResponseDto updateMessage(UUID id, MessageUpdateRequestDto request) {
		Message message = messageRepository.findById(id)
			.orElseThrow(() -> new MessageNotFoundException("Message not found with id: " + id.toString()));

		message.update(
			request.getMessage()
		);

		Message updatedMessage = messageRepository.save(message);
		return MessageUpdateResponseDto.toResponseDto(updatedMessage);
	}

	@Caching(evict = {
		@CacheEvict(value = "messageCache", key = "#id.toString()"),
		@CacheEvict(value = "messageSearchCache", allEntries = true)
	})
	public void deleteMessage(UUID id) {
		Message message = messageRepository.findById(id)
			.orElseThrow(() -> new MessageNotFoundException("Message not found with id: " + id.toString()));

		// TODO: deleted_by 구현 시 수정
		message.delete(UUID.randomUUID());
		messageRepository.save(message);
	}

	@Cacheable(value = "messageSearchCache",
		key = "{#request.id, #request.message, #request.page, #request.size, #request.sortOption?.name(), #request.sortOrder?.name()}"
	)
	public PagedMessageResponseDto getMessage(MessageSearchRequestDto request) {

		Page<Message> messagePage = messageRepository.findAll(request);

		Page<PagedMessageItemResponseDto> dtoPage = messagePage.map(PagedMessageItemResponseDto::toResponseDto);

		return PagedMessageResponseDto.toResponseDto(dtoPage, request.getSortOption());
	}

}
