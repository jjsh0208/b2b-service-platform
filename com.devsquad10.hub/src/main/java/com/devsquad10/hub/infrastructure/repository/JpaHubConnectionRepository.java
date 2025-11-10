package com.devsquad10.hub.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devsquad10.hub.domain.model.HubConnection;

@Repository
public interface JpaHubConnectionRepository extends JpaRepository<HubConnection, UUID> {
	List<HubConnection> findAllByActiveTrue();
}
