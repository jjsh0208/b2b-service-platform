package com.devsquad10.user.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.devsquad10.user.domain.model.User;
import com.devsquad10.user.domain.model.UserRoleEnum;

public interface UserRepository {
	Optional<Object> findByUsername(String username);

	User save(User user);

	Optional<Object> findByIdAndDeletedAtIsNull(UUID id);

	Page<User> findByUsernameContainingAndRole(String query, UserRoleEnum userRoleEnum, Pageable pageable);

	Optional<Object> findByEmail(String email);

	Optional<Object> findBySlackId(String slackId);
}
