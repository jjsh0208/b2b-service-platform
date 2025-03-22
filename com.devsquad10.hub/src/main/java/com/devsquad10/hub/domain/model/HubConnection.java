package com.devsquad10.hub.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
public class HubConnection {
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

	// TODO: Audit Fields 분리
	@Column(name = "created_at", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt;

	@Column(name = "created_by", nullable = false, updatable = false)
	private UUID createdBy;

	@Column(name = "updated_at")
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime updatedAt;

	@Column(name = "updated_by")
	private UUID updatedBy;

	@Column(name = "deleted_at")
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime deletedAt;

	@Column(name = "deleted_by")
	private UUID deletedBy;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();

		// TODO: created_by
		this.createdBy = UUID.randomUUID();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();

		// TODO: updated_by
	}
}
