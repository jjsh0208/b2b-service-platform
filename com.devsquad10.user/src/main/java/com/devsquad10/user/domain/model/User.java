package com.devsquad10.user.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.devsquad10.user.application.dto.UserRequestDto;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "p_users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "user_id", updatable = false, nullable = false)
	private UUID id;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false, unique = true)
	private String slackId;

	@Column(nullable = false)
	@Enumerated(value = EnumType.STRING)
	private UserRoleEnum role;

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

	public User(UserRequestDto requestDto, String password) {
		this.username = requestDto.getUsername();
		this.password = password;
		this.email = requestDto.getEmail();
		this.slackId = requestDto.getSlackId();
		this.role = requestDto.getRole();
	}

	public void update(UserRequestDto requestDto) {
		this.username = requestDto.getUsername();
		this.email = requestDto.getEmail();
		this.slackId = requestDto.getSlackId();
		this.role = requestDto.getRole();
	}

	public void delete(UUID id) {
		this.deletedAt = LocalDateTime.now();
		this.deletedBy = String.valueOf(id);
	}
}
