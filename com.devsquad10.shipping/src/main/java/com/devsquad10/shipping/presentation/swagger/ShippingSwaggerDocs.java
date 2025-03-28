package com.devsquad10.shipping.presentation.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD}) // 메서드에만 적용할 수 있음
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지됨
@Documented // javadoc 과 같은 문서에 포함되도록 지정
public @interface ShippingSwaggerDocs {

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송 내역 조회", description = "배송 ID를 통해 배송 내역을 조회합니다.")
	@Parameters({
		@Parameter(
			name = "orderId",
			description = "조회할 배송 데이터의 orderId",
			example = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
			required = true,
			schema = @Schema(type = "string", format = "uuid"),
			in = ParameterIn.PATH),
		@Parameter(
			name ="X-User-Id",
			description = "사용자 ID",
			required =true,
			in =ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"),
			example ="54ba053f-35a0-4c1a-919e-aa8666f1c220")
	})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "배송 ID가 존재하지 않음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface GetShippingById {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송 목록 검색", description = "검색 조건을 기반으로 배송 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송 조회 성공"),
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
	@interface SearchShipping {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송 상태 수정", description = "배송의 상태를 변경합니다.")
	@Parameters({
		@Parameter(
			name = "id",
			description = "배송 상태를 수정할 배송의 UUID",
			example = "ed9fb44f-734a-42b7-94e6-4adefe84475f",
			required = true,
			schema = @Schema(type = "string", format = "uuid"),
			in = ParameterIn.PATH
			),
		@Parameter(
			name ="X-User-Id",
			description ="사용자 ID",
			required =true,
			in =ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"),
			example ="54ba053f-35a0-4c1a-919e-aa8666f1c220")
	})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송 상태 수정 성공"),
		@ApiResponse(responseCode = "403", description = "수정 권한 없음"),
		@ApiResponse(responseCode = "404", description = "배송 ID가 존재하지 않음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface StatusUpdateShipping {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송 삭제", description = "배송 정보를 논리적으로 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송 삭제 성공"),
		@ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
		@ApiResponse(responseCode = "404", description = "배송 ID가 존재하지 않음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameters({
		@Parameter(
			name = "id",
			description = "배송 상태를 수정할 배송의 UUID",
			example = "ed9fb44f-734a-42b7-94e6-4adefe84475f",
			required = true,
			schema = @Schema(type = "string", format = "uuid"),
			in = ParameterIn.PATH),
		@Parameter(
			name ="X-User-Id",
			description ="사용자 ID",
			required =true,
			in =ParameterIn.HEADER,
			schema = @Schema(type = "string", format = "uuid"),
			example ="54ba053f-35a0-4c1a-919e-aa8666f1c220")
	})
	@interface DeleteShippingForOrder {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송 데이터 존재 여부 확인", description = "주어진 orderId에 해당하는 배송 데이터가 존재하는지 확인합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "존재 여부 확인 성공"),
		@ApiResponse(responseCode = "404", description = "배송 내역이 존재하지 않음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "orderId",
		description = "확인할 배송 데이터의 orderId",
		required = true,
		schema = @Schema(type = "string", format = "uuid"),
		in = ParameterIn.PATH,
		example = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
	)
	@interface IsShippingDataExists {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "배송 데이터 요청", description = "주어진 orderId에 해당하는 배송 데이터를 요청합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "배송 데이터 요청 성공"),
		@ApiResponse(responseCode = "404", description = "배송 내역이 존재하지 않음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "orderId",
		description = "요청할 배송 데이터의 orderId",
		example = "f47ac10b-58cc-4372-a567-0e02b2c3d479",
		required = true,
		schema = @Schema(type = "string", format = "uuid"),
		in = ParameterIn.PATH
	)
	@interface GetShippingClientData {}
}
