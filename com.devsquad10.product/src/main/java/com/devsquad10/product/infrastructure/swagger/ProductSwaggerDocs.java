package com.devsquad10.product.infrastructure.swagger;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target({ElementType.METHOD}) // 메서드에만 적용할 수 있음
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지됨
@Documented // javadoc 과 같은 문서에 포함되도록 지정
public @interface ProductSwaggerDocs {

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "상품 등록", description = "사용자에게서 받은 정보로 상품를 등록합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "상품 등록 성공"),
		@ApiResponse(responseCode = "404", description = "잘못된 공급업체ID 전달됨"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "X-User-Id",
		description = "사용자 고유 ID(UUID 형식)",
		required = true,
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", format = "uuid"),
		example = "550e8400-e29b-41d4-a716-446655440000"
	)
	@interface CreateProduct {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "상품 조회", description = "특정 상품를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "상품 조회 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지않는 상품를 조회 시도함"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "X-User-Id",
		description = "사용자 고유 ID(UUID 형식)",
		required = true,
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", format = "uuid"),
		example = "550e8400-e29b-41d4-a716-446655440000"
	)
	@interface GetProductById {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "상품 검색", description = "검색어(q) 및 카테고리를 기준으로 업체를 검색합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "검색 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "X-User-Id",
		description = "사용자 고유 ID(UUID 형식)",
		required = true,
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", format = "uuid"),
		example = "550e8400-e29b-41d4-a716-446655440000"
	)
	@Parameter(
		name = "q",
		description = "검색어",
		required = false,
		schema = @Schema(type = "string"),
		example = "커피"
	)
	@Parameter(
		name = "category",
		description = "검색 카테고리 (예: 상품명, 공급 업체 ID, 허브 ID 등)",
		required = false,
		schema = @Schema(type = "string"),
		example = "name"
	)
	@Parameter(
		name = "page",
		description = "페이지 번호 (0부터 시작)",
		required = false,
		schema = @Schema(type = "integer"),
		example = "0"
	)
	@Parameter(
		name = "size",
		description = "페이지 크기",
		required = false,
		schema = @Schema(type = "integer"),
		example = "10"
	)
	@Parameter(
		name = "sort",
		description = "정렬 기준 필드",
		required = false,
		schema = @Schema(type = "string"),
		example = "createAt"
	)
	@Parameter(
		name = "order",
		description = "정렬 방식 (asc/desc)",
		required = false,
		schema = @Schema(type = "string"),
		example = "desc"
	)
	@interface SearchProducts {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "상품 수정", description = "특정 상품를 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "상품 수정 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지않는 상품를 조회 시도 하거나 잘못된 공급업체 ID가 전달됨"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "X-User-Id",
		description = "사용자 고유 ID(UUID 형식)",
		required = true,
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", format = "uuid"),
		example = "550e8400-e29b-41d4-a716-446655440000"
	)
	@interface UpdateProduct {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "상품 삭제", description = "특정 상품를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "상품 수정 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지않는 상품를 조회 시도함"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "X-User-Id",
		description = "사용자 고유 ID(UUID 형식)",
		required = true,
		in = ParameterIn.HEADER,
		schema = @Schema(type = "string", format = "uuid"),
		example = "550e8400-e29b-41d4-a716-446655440000"
	)
	@interface DeleteProduct {
	}
}
