package com.devsquad10.hub.presentation.documentation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.devsquad10.hub.infrastructure.client.dto.HubFeignClientGetRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HubRouterSwaggerDocs {

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "허브 경로 생성", description = "두 허브 간의 이동 경로를 생성합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "허브 경로 생성 성공"),
		@ApiResponse(responseCode = "400", description = "유효성 검증 실패 또는 존재하지 않는 허브"),
		@ApiResponse(responseCode = "403", description = "허브 경로 생성 권한 없음 (마스터 관리자만 가능)"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface CreateHubRoute {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "허브 경로 조회", description = "특정 허브 경로의 상세 정보를 조회합니다.")
	@Parameters({
		@Parameter(name = "id", description = "조회할 허브 경로 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "허브 경로 조회 성공"),
		@ApiResponse(responseCode = "404", description = "허브 경로를 찾을 수 없음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface GetHubRoute {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "허브 경로 검색", description = "검색 조건에 맞는 허브 경로 목록을 페이징하여 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "허브 경로 검색 성공"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface SearchHubRoutes {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "허브 경로 수정", description = "허브 간 이동 경로 정보를 수정합니다.")
	@Parameters({
		@Parameter(name = "id", description = "수정할 허브 경로 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "허브 경로 수정 성공"),
		@ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
		@ApiResponse(responseCode = "403", description = "허브 경로 수정 권한 없음 (마스터 관리자만 가능)"),
		@ApiResponse(responseCode = "404", description = "허브 경로를 찾을 수 없음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface UpdateHubRoute {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "허브 경로 삭제", description = "허브 간 이동 경로를 논리적으로 삭제합니다.")
	@Parameters({
		@Parameter(name = "id", description = "삭제할 허브 경로 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "허브 경로 삭제 성공"),
		@ApiResponse(responseCode = "403", description = "허브 경로 삭제 권한 없음 (마스터 관리자만 가능)"),
		@ApiResponse(responseCode = "404", description = "허브 경로를 찾을 수 없음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface DeleteHubRoute {}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(
		summary = "허브 경로 정보 조회 (내부 API)",
		description = "출발 허브와 도착 허브 간의 경로 정보를 조회합니다. 서비스 간 내부 통신용 API입니다."
	)
	@Parameters({
		@Parameter(
			name = "departureHubId",
			description = "출발 허브 ID",
			required = true,
			example = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
		),
		@Parameter(
			name = "destinationHubId",
			description = "도착 허브 ID",
			required = true,
			example = "e37bc10b-48cc-4372-b567-0e02b2c3d123"
		)
	})
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "허브 경로 정보 조회 성공",
			content = @Content(
				mediaType = "application/json",
				array = @ArraySchema(schema = @Schema(implementation = HubFeignClientGetRequest.class))
			)
		),
		@ApiResponse(responseCode = "404", description = "출발지 또는 도착지 허브를 찾을 수 없음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface getHubRouteInfo {}
}
