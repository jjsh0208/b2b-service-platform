package com.devsquad10.order.infrastructure.swgger;

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
public @interface OrderSwaggerDocs {

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "주문 접수", description = "주문을 접수합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "주문 접수 성공"),
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
	@interface CreateOrder {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "주문 조회", description = "특정 주문을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "주문 조회 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지않는 주문을 조회 시도함"),
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
	@interface GetOrderById {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "주문 검색", description = "검색어(q) 및 카테고리를 기준으로 업체를 검색합니다.")
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
		description = "검색 카테고리 (예: 공급업체ID, 수령업체ID, 상품ID, 배송ID 등)",
		required = false,
		schema = @Schema(type = "string"),
		example = "550e8400-e29b-41d4-a716-446655440000"
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
	@interface searchOrders {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "주문 수정", description = "특정 수정을 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "주문 수정 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청으로 인한 오류 (예: 잘못된 요청 형식, 필수 값 누락 등)"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 주문을 조회 시도함"),
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
	@interface UpdateOrder {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(summary = "주문 삭제", description = "특정 주문을 논리적 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "주문 삭제 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 주문을 조회 시도함"),
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
	@interface DeleteOrder {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(
		summary = "주문 상태 변경 - 배송 완료",
		description = "주문의 배송 상태를 '배송 완료'로 변경합니다. 배송 ID를 통해 해당 주문을 찾고, 상태를 업데이트합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "주문 상태 변경 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 배송 ID를 조회 시도함"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "shippingId",
		description = "주문 배송을 위한 고유 배송 ID(UUID 형식). 배송 상태를 변경할 주문을 식별합니다.",
		required = true,
		in = ParameterIn.PATH,
		schema = @Schema(type = "string", format = "uuid"),
		example = "550e8400-e29b-41d4-a716-446655440000"
	)
	@interface UpdateOrderStatusToShipped {
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Operation(
		summary = "주문 상품 상세 조회",
		description = "주문 ID를 통해 주문 상품의 상세 정보를 조회합니다. 주문에 포함된 상품의 이름과 수량을 반환합니다."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "주문 상품 상세 조회 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 주문 ID를 조회 시도함"),
		@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@Parameter(
		name = "id",
		description = "주문을 식별하는 고유 주문 ID(UUID 형식). 상품 정보를 조회하려는 주문을 식별합니다.",
		required = true,
		in = ParameterIn.PATH,
		schema = @Schema(type = "string", format = "uuid"),
		example = "550e8400-e29b-41d4-a716-446655440000"
	)
	@interface GetOrderProductDetails {
	}
}
