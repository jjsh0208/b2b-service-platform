package com.devsquad10.product.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.product.application.dto.PageProductResponseDto;
import com.devsquad10.product.application.dto.ProductReqDto;
import com.devsquad10.product.application.dto.ProductResDto;
import com.devsquad10.product.application.exception.ProductNotFoundException;
import com.devsquad10.product.domain.enums.ProductStatus;
import com.devsquad10.product.domain.model.Product;
import com.devsquad10.product.domain.repository.ProductQuerydslRepository;
import com.devsquad10.product.domain.repository.ProductRepository;
import com.devsquad10.product.infrastructure.client.CompanyClient;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductQuerydslRepository productQuerydslRepository;
	private final CompanyClient companyClient;

	@CachePut(cacheNames = "productCache", key = "#result.id")
	public ProductResDto createProduct(ProductReqDto productReqDto) {

		// 특정 업체 존재 유무 확인
		// feign client
		UUID hubId = companyClient.findSupplierHubIdByCompanyId(productReqDto.getSupplierId());

		if (hubId == null)
			throw new EntityNotFoundException("Supplier Fot Found By Id : " + productReqDto.getSupplierId());
		// 업체가 존재하면 그 업체가 소속한 허브 id 등록

		return productRepository.save(Product.builder()
			.name(productReqDto.getName())
			.description(productReqDto.getDescription())
			.price(productReqDto.getPrice())
			.quantity(productReqDto.getQuantity())
			.supplierId(productReqDto.getSupplierId())
			.hubId(hubId)
			.status(ProductStatus.AVAILABLE)
			.build()).toResponseDto();
	}

	@Cacheable(cacheNames = "productCache", key = "#id")
	@Transactional(readOnly = true)
	public ProductResDto getProductById(UUID id) {
		return productRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ProductNotFoundException("Product Not Found By Id : " + id))
			.toResponseDto();
	}

	@Cacheable(cacheNames = "productSearchCache", key = "#q + '-' + #category + '-' + #page + '-' + #size")
	public PageProductResponseDto searchProducts(String q, String category, int page, int size, String sort,
		String order) {

		Page<Product> productPages = productQuerydslRepository.findAll(q, category, page, size, sort, order);

		Page<ProductResDto> productResDtoPages = productPages.map(Product::toResponseDto);

		return PageProductResponseDto.toResponse(productResDtoPages);
	}

	@CachePut(cacheNames = "productCache", key = "#id")
	@Caching(evict = {
		@CacheEvict(cacheNames = "productSearchCache", allEntries = true)
	})
	public ProductResDto updateProduct(UUID id, ProductReqDto productReqDto) {
		Product targetProduct = productRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ProductNotFoundException("Product Not Found By Id :" + id));

		return productRepository.save(targetProduct.toBuilder()
			.name(productReqDto.getName())
			.description(productReqDto.getDescription())
			.price(productReqDto.getPrice())
			.quantity(productReqDto.getQuantity())
			.supplierId(productReqDto.getSupplierId())
			.hubId(productReqDto.getHubId())
			.updatedAt(LocalDateTime.now())
			.updatedBy("사용자")
			.build()).toResponseDto();
	}

	@Caching(evict = {
		@CacheEvict(cacheNames = "productCache", key = "#id"),
		@CacheEvict(cacheNames = "productSearchCache", key = "#id")
	})
	public void deleteProduct(UUID id) {
		Product targetProduct = productRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ProductNotFoundException("Product Not Found By Id :" + id));

		productRepository.save(targetProduct.toBuilder()
			.deletedAt(LocalDateTime.now())
			.deletedBy("사용자")
			.build());
	}
}
