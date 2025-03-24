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

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/company")
public class CompanyController {

	private final CompanyService companyService;

	@PostMapping
	public ResponseEntity<CompanyResponse<CompanyResDto>> createCompany(
		@RequestBody CompanyReqDto companyReqDto, @RequestHeader("X-User-Id") String userId) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(CompanyResponse.success(HttpStatus.OK.value(), companyService.createCompany(companyReqDto, userId)));

	}

	@GetMapping("/{id}")
	public ResponseEntity<CompanyResponse<CompanyResDto>> getCompanyById(@PathVariable("id") UUID id) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(CompanyResponse.success(HttpStatus.OK.value(), companyService.getCompanyById(id)));
	}

	@GetMapping("/search")
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
	public ResponseEntity<CompanyResponse<CompanyResDto>> updateCompany(@PathVariable("id") UUID id,
		@RequestBody CompanyReqDto companyReqDto) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(CompanyResponse.success(HttpStatus.OK.value(), companyService.updateCompany(id, companyReqDto)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<CompanyResponse<String>> deleteCompany(@PathVariable("id") UUID id) {
		companyService.deleteCompany(id);
		return ResponseEntity.status(HttpStatus.OK)
			.body(CompanyResponse.success(HttpStatus.OK.value(), "Company Deleted successfully"));
	}

	@GetMapping("/exists/{uuid}")
	public UUID findSupplierHubIdByCompanyId(@PathVariable("uuid") UUID uuid) {
		return companyService.findSupplierHubIdByCompanyId(uuid);  // 존재하면 hubId, 없으면 null
	}

	@GetMapping("/address/{id}")
	public String findRecipientAddressByCompanyId(@PathVariable("id") UUID id) {
		return companyService.findRecipientAddressByCompanyId(id);
	}

	@GetMapping("/info/{id}")
	public ShippingCompanyInfoDto findShippingCompanyInfo(@PathVariable("id") UUID id) {
		return companyService.findShippingCompanyInfo(id);
	}
}
