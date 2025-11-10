package com.devsquad10.hub.domain.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 허브 연결 정보
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "p_hub_connection")
public class HubConnection extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hub_id", nullable = false)
	private Hub hub;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "connected_hub_id", nullable = false)
	private Hub connectedHub;

	// 연결 가중치 (이동 시간)
	private Integer weight;

	// 연결 상태
	private Boolean active;
}
