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

	/**
	 * 상품 등록 기능
	 *
	 * 주어진 업체 요청 DTO( CompanyReqDto )를 기반으로 새 상품을 등록한다.
	 * 1. 공급업체 ID가 존재하는 확인
	 * 2. 존재하고 공급업체가 맞으면 상품 정보를 저장 후 DTO ( CompanyResDto )로 반환
	 *
	 * 이 과정에서 공급업체가 존재하는지 확인하기 위해 `companyClient`를 통해 공급업체의 허브 ID를 조회한다.
	 * 만약 공급업체가 존재하지 않으면 `EntityNotFoundException`을 발생시킨다.
	 * 공급업체가 존재할 경우 상품 정보를 DB에 저장하고, 저장된 상품 정보를 `ProductResDto` 형식으로 반환한다.
	 * @param productReqDto 상품 등록에 필요한 정보를 포함한 DTO
	 * @param userId 요청을 수행하는 사용자의 ID
	 * @return 생성된 상품 정보를 담은 ProductResDto 객체
	 * @throws EntityNotFoundException 허브 ID가 존재하지 않을 경우 예외 발생
	 */
	@CachePut(cacheNames = "productCache", key = "#result.id")
	public ProductResDto createProduct(ProductReqDto productReqDto, UUID userId) {

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
			.createdBy(userId)
			.build()).toResponseDto();
	}

	/**
	 * 상품 조회 기능
	 *
	 * 특정 ID를 기반으로 상품을 조회한다. (캐싱 적용)
	 * 상품 조회 시 캐시에서 먼저 데이터를 확인하고, 캐시가 없으면 DB에서 조회하여 반환한다.
	 *
	 * 이 과정에서, `@Cacheable`을 사용하여 상품 정보를 캐시하며, 해당 ID로 조회된 상품이 없다면 `ProductNotFoundException`을 발생시킨다.
	 * 캐시가 존재할 경우, 캐시된 상품 데이터를 빠르게 반환한다.
	 *
	 * @param id 조회할 상품의 ID
	 * @return 조회된 상품 정보를 담은 ProductResDto 객체
	 * @throws ProductNotFoundException 해당 ID에 대한 상품이 존재하지 않을 경우 예외 발생
	 */
	@Cacheable(cacheNames = "productCache", key = "#id")
	@Transactional(readOnly = true)
	public ProductResDto getProductById(UUID id) {
		return productRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ProductNotFoundException("Product Not Found By Id : " + id))
			.toResponseDto();
	}

	/**
	 * 상품 검색 기능
	 *
	 * 주어진 조건을 기반으로 상품 목록을 검색한다. (캐싱 적용)
	 * 검색 조건에 따라 상품을 조회하고, 결과를 페이징 처리하여 반환한다.
	 *
	 * 이 과정에서 `@Cacheable`을 사용하여 검색 조건에 맞는 상품 목록을 캐시한다. 캐시가 존재하면 빠르게 반환하며,
	 * 캐시가 없을 경우 DB에서 검색하여 결과를 반환한다.
	 *
	 * @param q 검색어
	 * @param category 카테고리 ( 상품명 , 공급 업체 ID, 담당 허브 ID )
	 * @param page 페이지 번호
	 * @param size 페이지 크기
	 * @param sort 정렬 기준
	 * @param order 정렬 순서 (ASC/DESC)
	 * @return 검색 결과를 포함한 PageProductResponseDto 객체
	 */
	@Cacheable(cacheNames = "productSearchCache", key = "#q + '-' + #category + '-' + #page + '-' + #size")
	public PageProductResponseDto searchProducts(String q, String category, int page, int size, String sort,
		String order) {

		Page<Product> productPages = productQuerydslRepository.findAll(q, category, page, size, sort, order);

		Page<ProductResDto> productResDtoPages = productPages.map(Product::toResponseDto);

		return PageProductResponseDto.toResponse(productResDtoPages);
	}

	/**
	 * 상품 정보 업데이트 기능
	 *
	 * 주어진 ID에 해당하는 상품 정보를 업데이트한다.
	 * 1. 먼저 해당 상품이 존재하는지 확인
	 * 2. 상품 상태와 수량에 따라 상태 변경 여부를 판단
	 * 3. 변경된 정보를 저장 후 반환
	 *
	 * 이 과정에서, 상품 상태가 'SOLD_OUT'인 경우 수량이 증가하면 상태를 'AVAILABLE'로 변경한다.
	 * 그런 후, 상품 정보를 업데이트하고, `@CachePut`과 `@Caching`을 통해 캐시를 갱신하며,
	 * `productSearchCache`를 비운다.
	 *
	 * @param id 업데이트할 상품의 ID
	 * @param productReqDto 업데이트할 상품 정보 DTO
	 * @param userId 요청을 수행하는 사용자의 ID
	 * @return 업데이트된 상품 정보를 담은 ProductResDto 객체
	 * @throws ProductNotFoundException 해당 ID에 대한 상품이 존재하지 않을 경우 예외 발생
	 */
	@CachePut(cacheNames = "productCache", key = "#id")
	@Caching(evict = {
		@CacheEvict(cacheNames = "productSearchCache", allEntries = true)
	})
	public ProductResDto updateProduct(UUID id, ProductReqDto productReqDto, UUID userId) {
		Product targetProduct = productRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ProductNotFoundException("Product Not Found By Id :" + id));

		UUID hubId = companyClient.findSupplierHubIdByCompanyId(productReqDto.getSupplierId());

		if (hubId == null)
			throw new EntityNotFoundException("Supplier Fot Found By Id : " + productReqDto.getSupplierId());

		// 상태 변경 로직 최적화
		int productQuantity = targetProduct.getQuantity();
		int newQuantity = productReqDto.getQuantity();

		// SOLD_OUT 상태에서 수량이 증가하면 상태 변경
		if (targetProduct.getStatus().equals(ProductStatus.SOLD_OUT) && productQuantity < newQuantity) {
			targetProduct = targetProduct.toBuilder()
				.status(ProductStatus.AVAILABLE)  // 상태 변경
				.build();
		}

		// 업데이트된 Product 저장
		targetProduct = targetProduct.toBuilder()
			.name(productReqDto.getName())
			.description(productReqDto.getDescription())
			.price(productReqDto.getPrice())
			.quantity(newQuantity)  // 수량 업데이트
			.supplierId(productReqDto.getSupplierId())
			.hubId(hubId)
			.updatedAt(LocalDateTime.now())
			.updatedBy(userId)
			.build();

		return productRepository.save(targetProduct).toResponseDto();
	}

	/**
	 * 상품 삭제 기능
	 *
	 * 주어진 ID에 해당하는 상품을 삭제한다. (삭제 날짜와 삭제자를 기록하여 'soft delete' 처리)
	 * 1. 주어진 ID에 해당하는 상품이 존재하는지 확인
	 * 2. 상품이 존재하면 해당 상품의 `deletedAt`과 `deletedBy` 값을 업데이트하여 상품을 삭제 처리
	 * 3. 삭제된 상품은 캐시에서 제거된다.
	 *
	 * 이 과정에서 `@Caching`을 사용하여 상품에 관련된 두 가지 캐시(`productCache`와 `productSearchCache`)를 갱신한다.
	 * `@CacheEvict`는 해당 상품의 캐시를 삭제하여, 삭제된 상품에 대한 캐시된 정보가 더 이상 사용되지 않도록 한다.
	 *
	 * @param id 삭제할 상품의 ID
	 * @param userId 요청을 수행한 사용자의 ID
	 * @throws ProductNotFoundException 해당 ID에 대한 상품이 존재하지 않을 경우 예외 발생
	 */
	@Caching(evict = {
		@CacheEvict(cacheNames = "productCache", key = "#id"),
		@CacheEvict(cacheNames = "productSearchCache", key = "#id")
	})
	public void deleteProduct(UUID id, UUID userId) {
		Product targetProduct = productRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new ProductNotFoundException("Product Not Found By Id :" + id));

		productRepository.save(targetProduct.toBuilder()
			.deletedAt(LocalDateTime.now())
			.deletedBy(userId)
			.build());
	}
}
