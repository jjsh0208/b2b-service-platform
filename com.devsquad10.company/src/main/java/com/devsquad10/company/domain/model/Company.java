package com.devsquad10.company.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.devsquad10.company.application.dto.CompanyResDto;
import com.devsquad10.company.domain.enums.CompanyTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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
@Table(name = "p_company")
public class Company {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column
	private UUID venderId;

	@Column
	private UUID hubId;

	@Column
	private String address;

	@Enumerated(EnumType.STRING)
	@Column
	private CompanyTypes type;

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

	public CompanyResDto toResponseDto() {
		return new CompanyResDto(
			this.id,
			this.name,
			this.venderId,
			this.hubId,
			this.address,
			this.type
		);
	}

}
