package com.devsquad10.product.presentation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devsquad10.product.application.dto.PageProductResponseDto;
import com.devsquad10.product.application.dto.ProductReqDto;
import com.devsquad10.product.application.dto.ProductResDto;
import com.devsquad10.product.application.dto.response.ProductResponse;
import com.devsquad10.product.application.service.ProductService;
import com.devsquad10.product.infrastructure.swagger.ProductSwaggerDocs;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Product API", description = "상품 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

	private final ProductService productService;

	@PostMapping
	@ProductSwaggerDocs.CreateProduct
	public ResponseEntity<ProductResponse<ProductResDto>> createProduct(@RequestBody ProductReqDto productReqDto,
		@RequestHeader("X-User-Id") UUID userId) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(ProductResponse.success(HttpStatus.OK.value(), productService.createProduct(productReqDto, userId)));

	}

	@GetMapping("/{id}")
	@ProductSwaggerDocs.GetProductById
	public ResponseEntity<ProductResponse<ProductResDto>> getProductById(@PathVariable("id") UUID id) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(ProductResponse.success(HttpStatus.OK.value(), productService.getProductById(id)));
	}

	@GetMapping("/search")
	@ProductSwaggerDocs.SearchProducts
	public ResponseEntity<ProductResponse<PageProductResponseDto>> searchProducts(
		@RequestParam(required = false) String q,
		@RequestParam(required = false) String category,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String order) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(ProductResponse.success(HttpStatus.OK.value(),
				productService.searchProducts(q, category, page, size, sort, order)));
	}

	@PatchMapping("/{id}")
	@ProductSwaggerDocs.UpdateProduct
	public ResponseEntity<ProductResponse<ProductResDto>> updateProduct(@PathVariable("id") UUID id,
		@RequestBody ProductReqDto productReqDto, @RequestHeader("X-User-Id") UUID userId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(ProductResponse.success(HttpStatus.OK.value(),
				productService.updateProduct(id, productReqDto, userId)));
	}

	@DeleteMapping("/{id}")
	@ProductSwaggerDocs.DeleteProduct
	public ResponseEntity<ProductResponse<String>> deleteProduct(@PathVariable("id") UUID id,
		@RequestHeader("X-User-Id") UUID userId) {
		productService.deleteProduct(id, userId);
		return ResponseEntity.status(HttpStatus.OK)
			.body(ProductResponse.success(HttpStatus.OK.value(), "Product Deleted successfully"));
	}

}
