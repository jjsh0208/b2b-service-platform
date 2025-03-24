package com.devsquad10.shipping.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.devsquad10.shipping.application.dto.message.ShippingResponseMessage;
import com.devsquad10.shipping.application.dto.response.ShippingResDto;
import com.devsquad10.shipping.domain.enums.ShippingStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "p_shipping")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Shipping {

	public Shipping() {
		this.historyList = new ArrayList<>();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column
	private ShippingStatus status;

	@Column(nullable = false)
	private UUID departureHubId;

	@Column(nullable = false)
	private UUID destinationHubId;

	@Column(nullable = false)
	private UUID orderId;

	@Column(nullable = false)
	private String address;

	@Column(nullable = false)
	private String recipientName;

	@Column(nullable = false)
	private String recipientSlackId;

	@Column
	private String requestDetails;

	@Column(nullable = true)
	private UUID companyShippingManagerId;

	@Column
	private Date deadLine;

	@OneToMany(mappedBy = "shipping", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ShippingHistory> historyList = new ArrayList<>();

	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@CreatedBy
	@Column(updatable = false, nullable = false)
	private String createdBy;

	@LastModifiedDate
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = true, insertable = false)
	private LocalDateTime updatedAt;

	@LastModifiedBy
	@Column(updatable = true, insertable = false)
	private String updatedBy;

	@Column
	private LocalDateTime deletedAt;

	@Column
	private String deletedBy;

	// TODO: user 설정
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.createdBy = "defaultUser"; // (예: SecurityContext에서 사용자 정보)
	}

	// TODO: user 설정
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
		this.updatedBy = "updateUser";
	}

	// TODO: 삭제유저 구현 예정
	// 소프트 삭제 처리
	public Shipping softDelete() {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = "deleteUser";

		return this;
	}

	public ShippingResDto toResponseDto() {
		return new ShippingResDto(
			this.id,
			this.status,
			this.orderId
		);
	}

	public ShippingResponseMessage toShippingResponseMessage() {
		return ShippingResponseMessage.builder()
			.shippingId(this.id)
			.orderId(this.orderId)
			.status("SUCCESS")
			.build();
	}

	// public void addShippingHistory(ShippingHistory shippingHistory) {
	// 	shippingHistory.setShipping(this); // 양방향 관계 설정
	// 	this.historyList.add(shippingHistory);
	// }
}
