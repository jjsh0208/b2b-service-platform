package com.devsquad10.company.presentation.controller;

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

import com.devsquad10.company.application.dto.CompanyReqDto;
import com.devsquad10.company.application.dto.CompanyResDto;
import com.devsquad10.company.application.dto.PageCompanyResponseDto;
import com.devsquad10.company.application.dto.ShippingCompanyInfoDto;
import com.devsquad10.company.application.dto.response.CompanyResponse;
import com.devsquad10.company.application.service.CompanyService;
import com.devsquad10.company.infrastructure.swagger.CompanySwaggerDocs;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Company API", description = "업체 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/company")
public class CompanyController {

	private final CompanyService companyService;

	@PostMapping
	@CompanySwaggerDocs.CreateCompany
	public ResponseEntity<CompanyResponse<CompanyResDto>> createCompany(
		@RequestBody CompanyReqDto companyReqDto, @RequestHeader("X-User-Id") UUID userId) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(CompanyResponse.success(HttpStatus.OK.value(), companyService.createCompany(companyReqDto, userId)));

	}

	@GetMapping("/{id}")
	@CompanySwaggerDocs.GetCompanyById
	public ResponseEntity<CompanyResponse<CompanyResDto>> getCompanyById(@PathVariable("id") UUID id) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(CompanyResponse.success(HttpStatus.OK.value(), companyService.getCompanyById(id)));
	}

	@GetMapping("/search")
	@CompanySwaggerDocs.SearchCompanies
	public ResponseEntity<CompanyResponse<PageCompanyResponseDto>> searchCompanies(
		@RequestParam(required = false) String q,
		@RequestParam(required = false) String category,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String order) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(CompanyResponse.success(HttpStatus.OK.value(),
				companyService.searchCompanies(q, category, page, size, sort, order)));

	}

	@PatchMapping("/{id}")
	@CompanySwaggerDocs.UpdateCompany
	public ResponseEntity<CompanyResponse<CompanyResDto>> updateCompany(@PathVariable("id") UUID id,
		@RequestBody CompanyReqDto companyReqDto, @RequestHeader("X-User-Id") UUID userId) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(CompanyResponse.success(HttpStatus.OK.value(),
				companyService.updateCompany(id, companyReqDto, userId)));
	}

	@DeleteMapping("/{id}")
	@CompanySwaggerDocs.DeleteCompany
	public ResponseEntity<CompanyResponse<String>> deleteCompany(@PathVariable("id") UUID id,
		@RequestHeader("X-User-Id") UUID userId) {
		companyService.deleteCompany(id, userId);
		return ResponseEntity.status(HttpStatus.OK)
			.body(CompanyResponse.success(HttpStatus.OK.value(), "Company Deleted successfully"));
	}

	@GetMapping("/exists/{uuid}")
	@CompanySwaggerDocs.FindSupplierHubId
	public UUID findSupplierHubIdByCompanyId(@PathVariable("uuid") UUID uuid) {
		return companyService.findSupplierHubIdByCompanyId(uuid);  // 존재하면 hubId, 없으면 null
	}

	@GetMapping("/address/{id}")
	@CompanySwaggerDocs.FindRecipientAddress
	public String findRecipientAddressByCompanyId(@PathVariable("id") UUID id) {
		return companyService.findRecipientAddressByCompanyId(id);
	}

	@GetMapping("/info/{id}")
	@CompanySwaggerDocs.FindShippingCompanyInfo
	public ShippingCompanyInfoDto findShippingCompanyInfo(@PathVariable("id") UUID id) {
		return companyService.findShippingCompanyInfo(id);
	}
}
