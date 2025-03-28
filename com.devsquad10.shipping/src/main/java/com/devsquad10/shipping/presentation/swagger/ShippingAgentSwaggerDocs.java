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
	@Operation(summary = "배송담당자 등록", description = "사용자 회원가입에서 배송담당자 등록을 처리합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송담당자 등록 성공"),
		@ApiResponse(responseCode = "404", description = "허브 ID 존재하지 않음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "X-User-Id",
		description = "사용자 ID",
		required = true,
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", format = "uuid"),
		example = "54ba053f-35a0-4c1a-919e-aa8666f1c220"
	)
	@interface CreateShippingAgent {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송담당자 정보 조회", description = "배송담당자 ID를 통해 배송담당자 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송담당자 조회 성공"),
		@ApiResponse(responseCode = "404", description = "배송담당자 ID가 존재하지 않음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameters({
		@Parameter(
			name = "shippingManagerId",
			description = "조회할 배송담당자의 UUID=회원의 UUID",
			example = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
			required = true,
			schema = @Schema(type = "string", format = "uuid"),
			in = ParameterIn.PATH
		),
		@Parameter(
			name = "X-User-Id",
			description = "사용자 ID",
			required = true,
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"),
			example = "54ba053f-35a0-4c1a-919e-aa8666f1c220"
		)
	})
	@interface GetShippingAgent {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송담당자 목록 검색", description = "검색 조건을 기반으로 배송담당자 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송담당자 검색 성공"),
		@ApiResponse(responseCode = "400", description = "잘못 요청된 파라미터"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "X-User-Id",
		description = "사용자 ID",
		required = true,
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", format = "uuid"),
		example = "54ba053f-35a0-4c1a-919e-aa8666f1c220"
	)
	@interface SearchShippingAgents {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송담당자 정보 수정", description = "사용자 정보 수정에서 배송담당자의 정보를 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송담당자 정보 수정 성공"),
		@ApiResponse(responseCode = "403", description = "수정 권한 없음"),
		@ApiResponse(responseCode = "404", description = "배송담당자 ID가 존재하지 않음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "X-User-Id",
		description = "사용자 ID",
		required = true,
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", format = "uuid"),
		example = "54ba053f-35a0-4c1a-919e-aa8666f1c220"
	)
	@interface InfoUpdateShippingAgent {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송 여부 상태 변경", description = "배송담당자의 배송 여부 상태를 변경합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송 여부 상태 수정 성공", content = @Content(schema = @Schema(implementation = ShippingAgentResponse.class))),
		@ApiResponse(responseCode = "403", description = "수정 권한 없음"),
		@ApiResponse(responseCode = "404", description = "배송담당자 ID가 존재하지 않음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameters({
		@Parameter(
			name = "shippingManagerId",
			description = "배송 여부 상태를 수정할 배송담당자의 UUID",
			example = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
			required = true,
			schema = @Schema(type = "string", format = "uuid"),
			in = ParameterIn.PATH
		),
		@Parameter(
			name = "isTransit",
			description = "배송 여부 상태 (true: 배송 중, false: 배송 대기)",
			example = "true",
			required = true,
			schema = @Schema(type = "string", format = "uuid"),
			in = ParameterIn.PATH
		),
		@Parameter(
			name = "X-User-Id",
			description = "사용자 ID",
			required = true,
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"),
			example = "54ba053f-35a0-4c1a-919e-aa8666f1c220"
		)
	})
	@interface TransitUpdateShippingAgent {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송담당자 삭제", description = "배송담당자 정보를 논리적으로 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송담당자 삭제 성공"),
		@ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
		@ApiResponse(responseCode = "404", description = "배송담당자 ID가 존재하지 않음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameters({
		@Parameter(
			name = "shippingManagerId",
			description = "삭제할 배송담당자의 UUID",
			required = true,
			schema = @Schema(type = "string", format = "uuid"),
			in = ParameterIn.PATH,
			example = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
		),
		@Parameter(
			name = "X-User-Id",
			description = "사용자 ID",
			required = true,
			in = ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"),
			example = "54ba053f-35a0-4c1a-919e-aa8666f1c220"
		)
	})
	@interface DeleteShippingAgentForUser {}
}