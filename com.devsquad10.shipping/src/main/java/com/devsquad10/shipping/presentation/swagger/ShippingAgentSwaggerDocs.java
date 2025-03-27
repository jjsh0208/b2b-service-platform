package com.devsquad10.shipping.presentation.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

import com.devsquad10.shipping.application.dto.ShippingAgentResponse;

@Target({ElementType.METHOD}) // 메서드에만 적용할 수 있음
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지됨
@Documented // javadoc 과 같은 문서에 포함되도록 지정
public @interface ShippingAgentSwaggerDocs {

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송담당자 등록", description = "배송담당자 회원가입 요청을 처리합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "회원 가입 성공"),
		@ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface CreateShippingAgent {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송담당자 정보 조회", description = "배송담당자 ID를 통해 배송담당자 정보를 조회합니다.")
	@Parameters({
		@Parameter(name = "shippingManagerId", description = "조회할 배송담당자의 UUID=회원의 UUID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "Authorization",
		description = "새로운 엑세스 토큰 (응답 헤더에서 반환)",
		required = false
	)
	@interface GetShippingAgent {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송담당자 목록 검색", description = "검색 조건을 기반으로 배송담당자 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "Authorization",
		description = "새로운 엑세스 토큰 (응답 헤더에서 반환)",
		required = false
	)
	@interface SearchShippingAgents {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송담당자 정보 수정", description = "배송담당자의 정보를 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송담당자 정보 수정 성공"),
		@ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
		@ApiResponse(responseCode = "403", description = "수정 권한 없음"),
		@ApiResponse(responseCode = "404", description = "해당 회원 없음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "Authorization",
		description = "새로운 엑세스 토큰 (응답 헤더에서 반환)",
		required = false
	)
	@interface InfoUpdateShippingAgent {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송 여부 상태 변경", description = "배송담당자의 배송 여부 상태를 변경합니다.")
	@Parameters({
		@Parameter(name = "shippingManagerId", description = "배송 여부 상태를 수정할 배송담당자의 UUID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479"),
		@Parameter(name = "isTransit", description = "배송 여부 상태 (true: 배송 중, false: 배송 대기)", example = "true"),
		@Parameter(name = "X-User-Id", description = "사용자 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479", in = ParameterIn.HEADER)
	})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송 여부 상태 수정 성공", content = @Content(schema = @Schema(implementation = ShippingAgentResponse.class))),
		@ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
		@ApiResponse(responseCode = "403", description = "수정 권한 없음"),
		@ApiResponse(responseCode = "404", description = "해당 회원 없음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "Authorization",
		description = "새로운 엑세스 토큰 (응답 헤더에서 반환)",
		required = false
	)
	@interface TransitUpdateShippingAgent {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송담당자 삭제", description = "배송담당자 정보를 논리적으로 삭제합니다.")
	@Parameters({
		@Parameter(name = "shippingManagerId", description = "삭제할 배송담당자의 UUID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송담당자 삭제 성공"),
		@ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
		@ApiResponse(responseCode = "404", description = "배송담당자 없음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "Authorization",
		description = "새로운 엑세스 토큰 (응답 헤더에서 반환)",
		required = false
	)
	@interface DeleteShippingAgentForUser {}
}