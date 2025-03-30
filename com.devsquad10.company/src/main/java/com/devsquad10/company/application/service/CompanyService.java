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
	 * 주어진 회사 요청 DTO( CompanyReqDto )를 기반으로 새 회사를 등록한다.
	 * 1. 허브 ID가 존재하는지 확인
	 * 2. 존재하면 회사 정보를 저장 후 DTO ( CompanyResDto )로 반환
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
	 * 특정 ID의 업체 조회 기능
	 *
	 * 주어진 업체 ID를 기반으로 업체를 조회합니다.
	 * 1. 업체 ID로 데이터를 조회
	 * 2. 존재하지 않으면 예외 발생
	 *
	 * @param id 조회할 ㅇ버체의 ID
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
	 * 검색어(q), 카테고리, 페이지네이션 정보(page, size, sort, order)를 기반으로 회사를 검색합니다.
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
	 * 특정 업체 ID를 기반으로 주어진 요청 DTO( companyReqDto )를 이용하여 업체를 업데이트합니다.
	 *
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
	 * 업체 삭제 기능 (소프트 삭제)
	 *
	 * 특정 업체 ID를 기반으로 해당 업체를 삭제(soft delete)합니다.
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
