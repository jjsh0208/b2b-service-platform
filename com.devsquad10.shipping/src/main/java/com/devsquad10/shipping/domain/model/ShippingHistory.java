package com.devsquad10.shipping.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.devsquad10.shipping.domain.enums.ShippingHistoryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.Setter;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "p_shipping_history")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne
	@JoinColumn(name = "shipping_id", nullable = false)
	private Shipping shipping;

	@Column
	private Integer shippingPathSequence;

	@Column
	private UUID departureHubId;

	@Column
	private UUID destinationHubId;

	@Column
	private UUID shippingManagerId;

	@Column
	private Double estDist;

	@Column
	private Integer estTime;

	@Column
	private Double actDist;

	@Column
	private Integer actTime;

	@Enumerated(EnumType.STRING)
	@Column
	private ShippingHistoryStatus historyStatus;

	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@CreatedBy
	@Column(updatable = false, nullable = false)
	private UUID createdBy;

	@LastModifiedDate
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = true, insertable = false)
	private LocalDateTime updatedAt;

	@LastModifiedBy
	@Column(updatable = true, insertable = false)
	private UUID updatedBy;

	@Column
	private LocalDateTime deletedAt;

	@Column
	private UUID deletedBy;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.createdBy = UUID.randomUUID();
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
		this.updatedBy = UUID.randomUUID();
	}

	// TODO: 삭제유저 구현 예정
	public ShippingHistory softDelete() {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = UUID.randomUUID();

		return this;
	}
}
