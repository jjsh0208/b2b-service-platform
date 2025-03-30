package com.devsquad10.company.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsquad10.company.application.dto.CompanyReqDto;
import com.devsquad10.company.application.dto.CompanyResDto;
import com.devsquad10.company.application.dto.PageCompanyResponseDto;
import com.devsquad10.company.application.dto.ShippingCompanyInfoDto;
import com.devsquad10.company.application.exception.CompanyNotFoundException;
import com.devsquad10.company.domain.enums.CompanyTypes;
import com.devsquad10.company.domain.model.Company;
import com.devsquad10.company.domain.repository.CompanyQuerydslRepository;
import com.devsquad10.company.domain.repository.CompanyRepository;
import com.devsquad10.company.infrastructure.client.HubClient;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

	private final CompanyRepository companyRepository;
	private final CompanyQuerydslRepository companyQuerydslRepository;
	private final HubClient hubClient;

	/**
	 * 업체 등록 기능
	 *
	 * 주어진 회사 요청 DTO( CompanyReqDto )를 기반으로 새 업체를 등록한다.
	 * 1. 허브 ID가 존재하는지 HubClient를 통해 확인한다.
	 * 2. 허브 ID가 유효하면, 해당 정보를 바탕으로 회사를 저장합니다.
	 * 3. 저장된 회사를 DTO( CompanyResDto ) 형식으로 변환하여 반환한다.
	 *
	 * 회사를 등록할 때 담당자 ID는 사용자가 로그인 후 전달한 유저 ID를 사용하여 저장한다.
	 *
	 * @param companyReqDto 업체 등록에 필요한 정보를 포함한 DTO
	 * @param userId 요청을 수행하는 사용자의 ID
	 * @return 생성된 회사 정보를 담은 CompanyResDto 객체
	 * @throws EntityNotFoundException 허브 ID가 존재하지 않을 경우 예외 발생
	 */
	@CachePut(cacheNames = "companyCache", key = "#result.id")
	public CompanyResDto createCompany(CompanyReqDto companyReqDto, UUID userId) {

		UUID hubId = companyReqDto.getHubId();

		//1. 허브 존재 유무 확인
		if (!hubClient.isHubExists(hubId)) {
			throw new EntityNotFoundException("Hub Not Found By Id : " + hubId);
		}

		// 담당자 id는 유저 완성 시 등록
		return companyRepository.save(Company.builder()
			.name(companyReqDto.getName())
			.venderId(userId)
			.hubId(hubId)
			.address(companyReqDto.getAddress())
			.type(companyReqDto.getType())
			.createdBy(userId)
			.build()).toResponseDto();
	}

	/**
	 * 업체 조회 기능
	 *
	 * 특정 ID를 기반으로 업체를 조회한다. (캐싱 적용)
	 * 업체 조회 시 캐시에서 먼저 데이터를 확인하고, 캐시가 없으면 DB에서 조회하여 반환한다.
	 *
	 * 이 과정에서, `@Cacheable`을 사용하여 업체 정보를 캐시하며, 해당 ID로 조회된 업체가 없다면 `CompanyNotFoundException`을 발생시킨다.
	 * 캐시가 존재할 경우, 캐시된 업체 데이터를 빠르게 반환한다.
	 *
	 * @param id 조회할 업체의 ID
	 * @return 조회된 업체 정보를 담은 CompanyResDto 객체
	 * @throws CompanyNotFoundException 해당 ID의 업체가 존재하지 않을 경우 예외 발생
	 */
	@Cacheable(cacheNames = "companyCache", key = "#id")
	@Transactional(readOnly = true)
	public CompanyResDto getCompanyById(UUID id) {
		return companyRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new CompanyNotFoundException("Company Not Found By  Id : " + id))
			.toResponseDto();
	}

	/**
	 * 업체 검색 기능
	 *
	 * 주어진 조건을 기반으로 업체 목록을 검색한다. ( 캐싱 적용 )
	 * 검색 조건에 따라 업체를 조회하고, 결과를 페이징 처리하여 반환한다.
	 *
	 * 이 과정에서 `@Cacheable`을 사용하여 검색 조건에 맞는 업체 목록을 캐시한다. 캐시가 존재하면 빠르게 반환하며,
	 * 캐시가 없을 경우 DB에서 검색하여 결과를 반환한다.
	 *
	 * @param q 검색어
	 * @param category 카테고리 ( 업체명 , 업체 주소, 업체 타입 )
	 * @param page 페이지 번호
	 * @param size 페이지 크기
	 * @param sort 정렬 기준
	 * @param order 정렬 순서 (ASC/DESC)
	 * @return 검색 결과를 포함한 PageCompanyResponseDto 객체
	 */
	@Cacheable(cacheNames = "companySearchCache", key = "#q + '-' + #category + '-' + #page + '-' + #size")
	@Transactional(readOnly = true)
	public PageCompanyResponseDto searchCompanies(String q, String category, int page, int size, String sort,
		String order) {

		Page<Company> companyPages = companyQuerydslRepository.findAll(q, category, page, size, sort, order);

		Page<CompanyResDto> companyResDtdPages = companyPages.map(Company::toResponseDto);

		return PageCompanyResponseDto.toResponse(companyResDtdPages);

	}

	/**
	 * 업체 정보 업데이트 기능
	 *
	 * 주어진 회사 요청 DTO( CompanyReqDto )를 기반으로 업체 정보를 업데이트한다
	 * 1. 허브 ID가 존재하는지 HubClient를 통해 확인한다.
	 * 2. 허브 ID가 유효하면, 해당 정보를 바탕으로 회사를 업데이트한다.
	 * 3. 업데이트된 회사를 DTO( CompanyResDto ) 형식으로 변환하여 반환한다.
	 *
	 * 업체 정보를 업데이트하고, `@CachePut`과 `@Caching`을 통해 캐시를 갱신하며,
	 * `companySearchCacheSearchCache`를 비운다.
	 * @param id 업데이트할 업체의 ID
	 * @param companyReqDto 업데이트할 정보가 담긴 DTO
	 * @param userId 요청을 수행하는 사용자의 ID
	 * @return 업데이트된 회사 정보를 담은 CompanyResDto 객체
	 * @throws CompanyNotFoundException 업체가 존재하지 않을 경우 예외 발생
	 * @throws EntityNotFoundException 허브 ID가 존재하지 않을 경우 예외 발생
	 */
	@CachePut(cacheNames = "companyCache", key = "#id")
	@Caching(evict = {
		@CacheEvict(cacheNames = "companySearchCache", allEntries = true)
	})
	public CompanyResDto updateCompany(UUID id, CompanyReqDto companyReqDto, UUID userId) {
		Company targetCompany = companyRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new CompanyNotFoundException("Company Not Found By  Id : " + id));

		UUID hubId = companyReqDto.getHubId();

		//1. 허브 존재 유무 확인
		if (!hubClient.isHubExists(hubId)) {
			throw new EntityNotFoundException("Hub Not Found By Id : " + hubId);
		}

		return companyRepository.save(targetCompany.toBuilder()
			.name(companyReqDto.getName())
			.hubId(hubId)
			.address(companyReqDto.getAddress())
			.type(companyReqDto.getType())
			.updatedAt(LocalDateTime.now())
			.updatedBy(userId)
			.build()).toResponseDto();
	}

	/**
	 * 업체 삭제 기능
	 *
	 * 주어진 ID에 해당하는 업체을 삭제한다. (삭제 날짜와 삭제자를 기록하여 'soft delete' 처리)
	 * 1. 주어진 ID에 해당하는 업체가 존재하는지 확인
	 * 2. 상품이 존재하면 해당 상품의 `deletedAt`과 `deletedBy` 값을 업데이트하여 상품을 삭제 처리
	 * 3. 삭제된 상품은 캐시에서 제거된다.
	 *
	 * 이 과정에서 `@Caching`을 사용하여 상품에 관련된 두 가지 캐시(`productCache`와 `productSearchCache`)를 갱신한다.
	 * `@CacheEvict`는 해당 상품의 캐시를 삭제하여, 삭제된 상품에 대한 캐시된 정보가 더 이상 사용되지 않도록 한다.
	 *
	 * @param id 삭제할 업체의 ID
	 * @param userId 요청을 수행하는 사용자의 ID
	 * @throws CompanyNotFoundException 업체가 존재하지 않을 경우 예외 발생
	 */
	@Caching(evict = {
		@CacheEvict(cacheNames = "companyCache", key = "#id"),
		@CacheEvict(cacheNames = "companySearchCache", key = "#id")
	})
	public void deleteCompany(UUID id, UUID userId) {
		Company targetCompany = companyRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new CompanyNotFoundException("Company Not Found By  Id : " + id));

		companyRepository.save(targetCompany.toBuilder()
			.deletedAt(LocalDateTime.now())
			.deletedBy(userId)
			.build());
	}

	/**
	 * 특정 업체가 공급업체( SUPPLIER )인지 확인하고 허브 ID를 반환
	 *
	 * @param id 업체의 ID
	 * @return 업체가 공급업체인 경우 해당 허브 ID, 아니라면 null 반환
	 */
	public UUID findSupplierHubIdByCompanyId(UUID id) {
		Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
			.orElse(null);
		return (company != null && company.getType().equals(CompanyTypes.SUPPLIER)) ? company.getHubId() : null;
	}

	/**
	 * 특정 업체가 수령업체( RECIPIENTS )인지 확인하고 주소를 반환
	 *
	 * @param id 업체의 ID
	 * @return 업체가 수령업체인 경우 해당 주소, 아니라면 null 반환
	 */
	public String findRecipientAddressByCompanyId(UUID id) {
		Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
			.orElse(null);
		return (company != null && company.getType().equals(CompanyTypes.RECIPIENTS)) ? company.getAddress() : null;
	}

	/**
	 * 특정 업체의 배송 관련 정보를 조회
	 *
	 * @param id 회사의 ID
	 * @return ShippingCompanyInfoDto 객체 (판매자 ID 및 허브 ID 포함), 없을 경우 null 반환
	 */
	public ShippingCompanyInfoDto findShippingCompanyInfo(UUID id) {
		return companyRepository.findByIdAndDeletedAtIsNull(id)
			.map(company -> new ShippingCompanyInfoDto(company.getVenderId(), company.getHubId()))
			.orElse(null); // 없을 경우 null 반환
	}
}
