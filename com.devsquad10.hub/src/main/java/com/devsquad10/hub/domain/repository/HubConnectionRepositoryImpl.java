package com.devsquad10.hub.domain.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.devsquad10.hub.domain.model.HubConnection;
import com.devsquad10.hub.infrastructure.repository.JpaHubConnectionRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class HubConnectionRepositoryImpl implements HubConnectionRepository {

	private final JpaHubConnectionRepository jpaHubConnectionRepository;

	@Override
	public List<HubConnection> findAllByActiveTrue() {
		return jpaHubConnectionRepository.findAllByActiveTrue();
	}
}
