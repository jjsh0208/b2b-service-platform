package com.devsquad10.hub.domain.repository;

import java.util.List;

import com.devsquad10.hub.domain.model.HubConnection;

public interface HubConnectionRepository {
	List<HubConnection> findAllByActiveTrue();
}
