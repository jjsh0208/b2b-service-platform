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

	@CachePut(cacheNames = "companyCache", key = "#result.id")
	public CompanyResDto createCompany(CompanyReqDto companyReqDto, String userId) {

		UUID venderId = UUID.fromString(userId);
		UUID hubId = companyReqDto.getHubId();

		//1. 허브 존재 유무 확인
		if (!hubClient.isHubExists(hubId)) {
			throw new EntityNotFoundException("Hub Not Found By Id : " + hubId);
		}

		// 담당자 id는 유저 완성 시 등록
		return companyRepository.save(Company.builder()
			.name(companyReqDto.getName())
			.venderId(venderId)
			.hubId(hubId)
			.address(companyReqDto.getAddress())
			.type(companyReqDto.getType())
			.build()).toResponseDto();
	}

	@Cacheable(cacheNames = "companyCache", key = "#id")
	@Transactional(readOnly = true)
	public CompanyResDto getCompanyById(UUID id) {
		return companyRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new CompanyNotFoundException("Company Not Found By  Id : " + id))
			.toResponseDto();
	}

	@Cacheable(cacheNames = "companySearchCache", key = "#q + '-' + #category + '-' + #page + '-' + #size")
	@Transactional(readOnly = true)
	public PageCompanyResponseDto searchCompanies(String q, String category, int page, int size, String sort,
		String order) {

		Page<Company> companyPages = companyQuerydslRepository.findAll(q, category, page, size, sort, order);

		Page<CompanyResDto> companyResDtdPages = companyPages.map(Company::toResponseDto);

		return PageCompanyResponseDto.toResponse(companyResDtdPages);

	}

	@CachePut(cacheNames = "companyCache", key = "#id")
	@Caching(evict = {
		@CacheEvict(cacheNames = "companySearchCache", allEntries = true)
	})
	public CompanyResDto updateCompany(UUID id, CompanyReqDto companyReqDto) {
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
			.updatedBy("사용자")
			.build()).toResponseDto();
	}

	@Caching(evict = {
		@CacheEvict(cacheNames = "companyCache", key = "#id"),
		@CacheEvict(cacheNames = "companySearchCache", key = "#id")
	})
	public void deleteCompany(UUID id) {
		Company targetCompany = companyRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new CompanyNotFoundException("Company Not Found By  Id : " + id));

		companyRepository.save(targetCompany.toBuilder()
			.deletedAt(LocalDateTime.now())
			.deletedBy("사용자")
			.build());
	}

	// 상품 등록 시 사용
	//findSupplierHubIdByCompanyId
	// getHubIdIfCompanyExists
	public UUID findSupplierHubIdByCompanyId(UUID id) {
		Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
			.orElse(null);
		return (company != null && company.getType().equals(CompanyTypes.SUPPLIER)) ? company.getHubId() : null;
	}

	// 주문 배송 메시지 생성 시 사용
	//findRecipientAddressByCompanyId
	// 원본 명 : getCompanyAddress
	public String findRecipientAddressByCompanyId(UUID id) {
		Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
			.orElse(null);
		return (company != null && company.getType().equals(CompanyTypes.RECIPIENTS)) ? company.getAddress() : null;
	}

	public ShippingCompanyInfoDto findShippingCompanyInfo(UUID id) {
		return companyRepository.findByIdAndDeletedAtIsNull(id)
			.map(company -> new ShippingCompanyInfoDto(company.getVenderId(), company.getHubId()))
			.orElse(null); // 없을 경우 null 반환
	}
}
