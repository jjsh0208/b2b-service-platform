package com.devsquad10.shipping.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.devsquad10.shipping.application.dto.response.ShippingAgentResDto;
import com.devsquad10.shipping.domain.enums.ShippingAgentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "p_shipping_agent")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ShippingAgent {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column
	private UUID hubId;

	@Column
	private UUID shippingManagerId;

	@Column
	private String shippingManagerSlackId;

	// 허브담당(HUB_DVL), 업체담당(COM_DVL)
	@Column
	@Enumerated(EnumType.STRING)
	private ShippingAgentType type;

	@Column(unique = true)
	private Integer shippingSequence;

	// 배송 진행 여부 : True(배송중), False(대기중)
	@Column
	private Boolean isTransit;

	@Column
	private Integer assignmentCount;

	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@CreatedBy
	@Column(updatable = false, nullable = false)
	private String createdBy;

	@LastModifiedDate
	@Temporal(TemporalType.TIMESTAMP)
	@Column(insertable = false)
	private LocalDateTime updatedAt;

	@LastModifiedBy
	@Column(insertable = false)
	private String updatedBy;

	@Column
	private LocalDateTime deletedAt;

	@Column
	private String deletedBy;

	// TODO: user 설정
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now(); // 현재 시간으로 설정
		this.createdBy = "defaultUser"; // 현재 사용자로 설정
	}

	// TODO: user 설정
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
		this.updatedBy = "updateUser";
	}

	// TODO: user 설정
	// 소프트 삭제 처리
	public ShippingAgent softDelete() {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = "deleteByUser";
		return this;
	}

	public ShippingAgentResDto toResponse() {
		return ShippingAgentResDto.builder()
			.id(id)
			.hubId(hubId)
			.shippingManagerId(shippingManagerId)
			.shippingManagerSlackId(shippingManagerSlackId)
			.type(type)
			.shippingSequence(shippingSequence)
			.isTransit(isTransit)
			.build();
	}

	public void increaseAssignmentCount() {
		if (assignmentCount == null) {
			assignmentCount = 0;
		}
		assignmentCount++;
	}
	public void decreaseAssignmentCount() {
		if (assignmentCount <= 0) {
			assignmentCount = 0;
		}
		assignmentCount--;
	}

	public void updateIsTransit() {
		this.isTransit = true;
	}

	public void isTransitToFalse() {
		this.isTransit = false;
	}
}
