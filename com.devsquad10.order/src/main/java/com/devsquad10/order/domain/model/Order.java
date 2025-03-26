package com.devsquad10.order.domain.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.devsquad10.order.application.dto.OrderResDto;
import com.devsquad10.order.application.dto.message.StockDecrementMessage;
import com.devsquad10.order.domain.enums.OrderStatus;

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
@Table(name = "p_order")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column
	private UUID supplierId; // 공급업체

	@Column
	private UUID recipientsId; // 수령업체

	@Column
	private UUID productId; // 상품 ID

	@Column
	private UUID shippingId; // 배송 ID;

	private String productName; // 상품명

	@Column(nullable = false)
	private Integer quantity; // 주문 수량

	private Integer totalAmount; // 총 금액

	@Column(nullable = false)
	private String requestDetails; // 요청사항

	@Column(nullable = false)
	private Date deadLine; // 납품기한일자

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status; // 주문 상태

	// 레코드 생성 일시
	@CreatedDate
	@Column(updatable = false, nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt;

	// 레코드 생성자
	@Column(updatable = false, nullable = false)
	private UUID createdBy;

	// 레코드 수정 일시
	@LastModifiedDate
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime updatedAt;

	// 레코드 수정자
	@Column
	private UUID updatedBy;

	// 레코드 삭제 일시
	@Column
	private LocalDateTime deletedAt;

	// 레코드 삭제 사용자
	@Column
	private UUID deletedBy;

	@PrePersist
	protected void onCreate() {
		LocalDateTime time = LocalDateTime.now();
		this.createdAt = time;
		this.updatedAt = time;
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public OrderResDto toResponseDto() {
		return new OrderResDto(
			this.id,
			this.supplierId,
			this.recipientsId,
			this.productId,
			this.shippingId,
			this.productName,
			this.quantity,
			this.totalAmount,
			this.requestDetails,
			this.deadLine,
			this.status
		);
	}

	public StockDecrementMessage toStockDecrementMessage() {
		return StockDecrementMessage.builder()
			.orderId(this.id)
			.productId(this.productId)
			.quantity(this.quantity)
			.build();
	}
}
