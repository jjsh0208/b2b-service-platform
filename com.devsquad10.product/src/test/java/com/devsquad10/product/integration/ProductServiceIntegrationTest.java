package com.devsquad10.product.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.devsquad10.product.application.dto.PageProductResponseDto;
import com.devsquad10.product.application.dto.ProductReqDto;
import com.devsquad10.product.application.dto.ProductResDto;
import com.devsquad10.product.application.exception.ProductNotFoundException;
import com.devsquad10.product.application.service.ProductService;
import com.devsquad10.product.domain.enums.ProductStatus;
import com.devsquad10.product.domain.model.Product;
import com.devsquad10.product.domain.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
public class ProductServiceIntegrationTest {

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductRepository productRepository;

	private Product testProduct;

	private UUID testProductId;
	private UUID testSupplierId;
	private UUID testHubId;

	@BeforeEach
	void serUp() {

		testSupplierId = UUID.fromString("5a96650f-b544-4370-9ae0-ada598fc74f1");
		testHubId = UUID.fromString("11111111-1111-1111-1111-111111111101");

		testProduct = Product.builder()
			.name("testProduct")
			.description("tetProduct good")
			.quantity(1000)
			.price(1000)
			.supplierId(testProductId)
			.hubId(testHubId)
			.status(ProductStatus.AVAILABLE)
			.createdBy("testUser")
			.build();

		productRepository.save(testProduct);
		testProductId = testProduct.getId();

	}

	@Test
	@DisplayName("Product 등록 - Success")
	void testCreateProductSuccess() {

		// Given
		String userId = "testUser";

		ProductReqDto productReqDto = new ProductReqDto("newProduct", "newProduct good", 1000, 500, testSupplierId);

		ProductResDto productResDto = productService.createProduct(productReqDto, userId);

		assertNotNull(productReqDto);
		assertEquals("newProduct", productResDto.getName());
		assertEquals("newProduct good", productResDto.getDescription());
	}

	@Test
	@DisplayName("Product 등록 - Fail - EntityNotFoundException")
	void testCreateProductFailEntityNotFoundException() {

		// Given
		String userId = "testUser";
		UUID FailSupplierId = UUID.randomUUID();

		ProductReqDto productReqDto = new ProductReqDto("newProduct", "newProduct good", 1000, 500, FailSupplierId);

		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
			productService.createProduct(productReqDto, userId);
		});

		assertEquals("Supplier Fot Found By Id : " + FailSupplierId, exception.getMessage());
	}

	@Test
	@DisplayName("Product 단일 조회 - Success")
	void testGetProductByIdSuccess() {
		// When
		ProductResDto result = productService.getProductById(testProductId);

		// Then
		assertNotNull(result);
		assertEquals(testProduct.getName(), result.getName());
	}

	@Test
	@DisplayName("Product 단일 조회 - Fail - ProductNotFound")
	void testGetProductByIdFail() {
		// Given
		UUID failCompanyId = UUID.randomUUID();

		// When & Then
		ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
			productService.getProductById(failCompanyId);
		});

		assertEquals("Product Not Found By Id : " + failCompanyId, exception.getMessage());
	}

	@Test
	@DisplayName("Product 검색 - Success")
	void testSearchProductsSuccess() {
		// Given
		String q = null;
		String category = null;
		int page = 0;
		int size = 10;
		String sort = "createdAt";
		String order = "desc";

		for (int i = 0; i < 10; i++) {
			Product testProduct = Product.builder()
				.name("Product " + i)
				.description("category1")
				.quantity(1000)
				.price(100)
				.status(ProductStatus.AVAILABLE)
				.createdBy("user" + i)
				.build();
			productRepository.save(testProduct);
		}

		// When
		PageProductResponseDto result1 = productService.searchProducts(q, category, page, size, sort, order);
		PageProductResponseDto result2 = productService.searchProducts(q, category, page, size, sort, order);

		// Then
		// 첫 번째 결과는 null이 아니어야 한다.
		assertNotNull(result1);

		// 두 번째 호출에서는 캐시된 결과이므로, 첫 번째 결과와 동일한지 비교
		assertNotNull(result2);
		// 결과의 내용이 동일한지 순서대로 비교
		assertEquals(result1.getContent().size(), result2.getContent().size());
		for (int i = 0; i < result1.getContent().size(); i++) {
			ProductResDto company1 = result1.getContent().get(i);
			ProductResDto company2 = result2.getContent().get(i);
			assertEquals(company1.getId(), company2.getId());
			assertEquals(company1.getName(), company2.getName());
			assertEquals(company1.getDescription(), company2.getDescription());
		}

		// 페이징 처리 테스트
		assertEquals(10, result1.getContent().size());
		assertEquals("Product 9", result1.getContent().get(0).getName());
	}

	@Test
	@DisplayName("Product 업데이트 - Success")
	void testUpdateProductSuccess() {
		// Given
		String userId = "testUser";

		ProductReqDto productReqDto = new ProductReqDto("updateProduct", "updateProduct good", 1000, 500,
			testSupplierId);

		// When
		ProductResDto result = productService.updateProduct(testProductId, productReqDto, userId);

		// Then
		assertNotNull(result);
		assertEquals("updateProduct", result.getName());
		assertEquals("updateProduct good", result.getDescription());

	}

	@Test
	@DisplayName("Product 업데이트 - Fail - ProductFotFound")
	void testUpdateProductFailProductNotFound() {
		// Given
		String userId = "testUser";
		UUID failProductId = UUID.randomUUID();

		ProductReqDto productReqDto = new ProductReqDto("updateProduct", "updateProduct good", 1000, 500,
			testSupplierId);

		// When & Then
		ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
			productService.updateProduct(failProductId, productReqDto, userId);
		});

		assertEquals("Product Not Found By Id :" + failProductId, exception.getMessage());

	}

	@Test
	@DisplayName("Product 업데이트 - Fail - EntityNotFoundException")
	void testUpdateProductFailEntityNotFoundException() {
		// Given
		String userId = "testUser";
		UUID failSupplierId = UUID.randomUUID();

		ProductReqDto productReqDto = new ProductReqDto("updateProduct", "updateProduct good", 1000, 500,
			failSupplierId);

		// When & Then
		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
			productService.updateProduct(testProductId, productReqDto, userId);
		});

		assertEquals("Supplier Fot Found By Id : " + failSupplierId, exception.getMessage());

	}

	@Test
	@DisplayName("Product 삭제 - Success")
	void testDeleteProductSuccess() {
		// Given
		String userId = "testUser";

		// When
		productService.deleteProduct(testProductId, userId);

		// Then
		Optional<Product> company = productRepository.findByIdAndDeletedAtIsNull(testProductId);
		assertTrue(company.isEmpty());
	}

	@Test
	@DisplayName("Product 삭제 - Fail - ProductNotFound")
	void testDeleteProductFailProductNotFound() {
		// Given
		String userId = "testUser";
		UUID failProductId = UUID.randomUUID();
		// When

		// When & Then
		ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
			productService.deleteProduct(failProductId, userId);
		});

		assertEquals("Product Not Found By Id :" + failProductId, exception.getMessage());
	}
}
