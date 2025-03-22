package com.devsquad10.product.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.devsquad10.product.application.dto.ProductResDto;
import com.devsquad10.product.domain.enums.ProductStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "p_product")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false)
	private Integer price;

	@Column
	private UUID supplierId;

	@Column
	private UUID hubId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ProductStatus status;

	// 레코드 생성 일시
	@CreatedDate
	@Column(updatable = false, nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt;

	// 레코드 생성자
	@Column(updatable = false, nullable = false)
	private String createdBy;

	// 레코드 수정 일시
	@LastModifiedDate
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime updatedAt;

	// 레코드 수정자
	@Column
	private String updatedBy;

	// 레코드 삭제 일시
	@Column
	private LocalDateTime deletedAt;

	// 레코드 삭제 사용자
	@Column
	private String deletedBy;

	@PrePersist
	protected void onCreate() {
		LocalDateTime time = LocalDateTime.now();
		this.createdAt = time;
		this.updatedAt = time;
		this.createdBy = "사용자";
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public ProductResDto toResponseDto() {
		return new ProductResDto(
			this.id,
			this.name,
			this.description,
			this.quantity,
			this.price,
			this.supplierId,
			this.hubId,
			this.status
		);
	}

	public void statusSoldOut() {
		this.status = ProductStatus.SOLD_OUT;
	}

	public void decreaseStock(int orderQuantity) {
		if (this.quantity < orderQuantity) {
			throw new IllegalArgumentException(
				"재고 부족: 현재 재고(" + this.quantity + ")보다 많은 수량(" + orderQuantity + ")을 감소시킬 수 없습니다.");
		}
		this.quantity -= orderQuantity;
	}
}
