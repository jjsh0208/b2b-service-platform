package com.devsquad10.message.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devsquad10.message.domain.model.Message;

@Repository
public interface JpaMessageRepository extends JpaRepository<Message, UUID>, MessageRepositoryCustom {
}
