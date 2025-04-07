package com.devsquad10.hub.presentation.documentation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HubSwaggerDocs {

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "허브 생성", description = "새로운 물류 허브를 생성합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "허브 생성 성공"),
		@ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
		@ApiResponse(responseCode = "403", description = "허브 생성 권한 없음 (마스터 관리자만 가능)"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface CreateHub {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "허브 조회", description = "특정 허브의 상세 정보를 조회합니다.")
	@Parameters({
		@Parameter(name = "id", description = "조회할 허브 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "허브 조회 성공"),
		@ApiResponse(responseCode = "404", description = "허브를 찾을 수 없음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface GetHub {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "허브 목록 조회", description = "검색 조건에 맞는 허브 목록을 페이징하여 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "허브 목록 조회 성공"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface SearchHubs {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "허브 수정", description = "특정 허브의 정보를 수정합니다.")
	@Parameters({
		@Parameter(name = "id", description = "수정할 허브 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "허브 수정 성공"),
		@ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
		@ApiResponse(responseCode = "403", description = "허브 수정 권한 없음 (마스터 관리자만 가능)"),
		@ApiResponse(responseCode = "404", description = "허브를 찾을 수 없음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface UpdateHub {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "허브 삭제", description = "특정 허브를 논리적으로 삭제합니다.")
	@Parameters({
		@Parameter(name = "id", description = "삭제할 허브 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "허브 삭제 성공"),
		@ApiResponse(responseCode = "403", description = "허브 삭제 권한 없음 (마스터 관리자만 가능)"),
		@ApiResponse(responseCode = "404", description = "허브를 찾을 수 없음"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@interface DeleteHub {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "허브 존재 확인", description = "특정 허브 ID가 존재하는지 확인합니다. FeignClient용 API입니다.")
	@Parameters({
		@Parameter(name = "uuid", description = "존재 여부를 확인할 허브 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	})
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "확인 성공", content = @Content(schema = @Schema(implementation = Boolean.class)))
	})
	@interface ExistsHub {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(
		summary = "허브 이름 조회 (내부 API)",
		description = "특정 허브 ID의 이름을 조회합니다. 서비스 간 내부 통신용 API입니다."
	)
	@Parameters({
		@Parameter(name = "id", description = "조회할 허브 ID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
	})
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "허브 이름 조회 성공",
			content = @Content(schema = @Schema(type = "string"), examples = @ExampleObject(value = "서울특별시 센터"))
		),
		@ApiResponse(responseCode = "404", description = "허브를 찾을 수 없음")
	})
	@interface GetHubName {
	}
}
