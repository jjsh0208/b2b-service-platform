package com.devsquad10.company.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.devsquad10.company.application.dto.CompanyReqDto;
import com.devsquad10.company.application.dto.CompanyResDto;
import com.devsquad10.company.application.dto.PageCompanyResponseDto;
import com.devsquad10.company.application.dto.ShippingCompanyInfoDto;
import com.devsquad10.company.application.exception.CompanyNotFoundException;
import com.devsquad10.company.application.service.CompanyService;
import com.devsquad10.company.domain.enums.CompanyTypes;
import com.devsquad10.company.domain.model.Company;
import com.devsquad10.company.domain.repository.CompanyRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
public class CompanyServiceIntegrationTest {

	@Autowired
	private CompanyService companyService;

	@Autowired
	private CompanyRepository companyRepository;

	private Company testSupplierCompany;
	private UUID testSupplierCompanyId;
	private UUID testSupplierCompanyVnederId;
	private UUID testSupplierCompanyHubId;

	private Company testRecipientsCompany;
	private UUID testRecipientsCompanyId;
	private UUID testRecipientsCompanyVnederId;
	private UUID testRecipientsCompanyHubId;

	@BeforeEach
	void setUp() {
		//테스트용 업체 생성 및 저장
		testSupplierCompanyVnederId = UUID.randomUUID();
		testSupplierCompanyHubId = UUID.fromString("11111111-1111-1111-1111-111111111101");

		testSupplierCompany = Company.builder()
			.name("testSupplierCompany")
			.venderId(testSupplierCompanyVnederId)
			.hubId(testSupplierCompanyHubId)
			.address("testSupplierCompanyAddress")
			.type(CompanyTypes.SUPPLIER)
			.createdBy(UUID.randomUUID())
			.build();

		testRecipientsCompanyVnederId = UUID.randomUUID();
		testRecipientsCompanyHubId = UUID.fromString("11111111-1111-1111-1111-111111111101");

		testRecipientsCompany = Company.builder()
			.name("testRecipientsCompany")
			.venderId(testRecipientsCompanyVnederId)
			.hubId(testRecipientsCompanyHubId)
			.address("testRecipientsCompanyAddress")
			.type(CompanyTypes.RECIPIENTS)
			.createdBy(UUID.randomUUID())
			.build();

		companyRepository.save(testSupplierCompany);
		companyRepository.save(testRecipientsCompany);

		testSupplierCompanyId = testSupplierCompany.getId();
		testRecipientsCompanyId = testRecipientsCompany.getId();

	}

	@Test
	@DisplayName("Company 등록 - Success")
	void testCreateCompanySuccess() {
		//Given
		CompanyReqDto companyReqDto = new CompanyReqDto("newSupplierCompany", UUID.randomUUID(),
			UUID.fromString("11111111-1111-1111-1111-111111111101"),
			"newSupplierAddress", CompanyTypes.SUPPLIER);

		// When: createCompany 메서드 호출
		CompanyResDto createdCompany = companyService.createCompany(companyReqDto, UUID.randomUUID());

		assertNotNull(createdCompany);
		assertEquals("newSupplierCompany", createdCompany.getName());
		assertEquals("newSupplierAddress", createdCompany.getAddress());
		assertEquals(CompanyTypes.SUPPLIER, createdCompany.getType());
	}

	@Test
	@DisplayName("Company 등록 - Fail - 허브가 존재하지 않으면 업체 등록 실패 테스트")
	void testCreateCompanyFailureHubNotFound() {
		// Given
		CompanyReqDto companyReqDto = new CompanyReqDto("newSupplierCompany", UUID.randomUUID(), UUID.randomUUID(),
			"newSupplierAddress", CompanyTypes.SUPPLIER);

		String userId = testSupplierCompanyVnederId.toString();  // 유효한 venderId (userId)

		// When & Then
		assertThrows(EntityNotFoundException.class, () -> {
			companyService.createCompany(companyReqDto, UUID.randomUUID());
		});
	}

	@Test
	@DisplayName("Company 단일 조회 - Success")
	void testGetCompanyByIdSuccess() {
		// When
		CompanyResDto result = companyService.getCompanyById(testSupplierCompanyId);

		// Then
		assertNotNull(result);
		assertEquals(testSupplierCompany.getAddress(), result.getAddress());

	}

	@Test
	@DisplayName("Company 단일 조회 - Fail - 잘못된 id를 받은경")
	void testGetCompanyById() {
		// Given
		UUID failCompanyId = UUID.randomUUID();

		// When & Then
		CompanyNotFoundException exception = assertThrows(CompanyNotFoundException.class, () -> {
			companyService.getCompanyById(failCompanyId);
		});

		assertEquals("Company Not Found By  Id : " + failCompanyId, exception.getMessage());

	}

	@Test
	@DisplayName("company 검색 - Success")
	void testSearchCompaniesSuccess() {
		// Given
		String q = null;
		String category = null;
		int page = 0;
		int size = 10;
		String sort = "createdAt";
		String order = "desc";

		for (int i = 0; i < 10; i++) {
			Company testCompany = Company.builder()
				.name("Company" + i)
				.address("category1")
				.createdBy(UUID.randomUUID())
				.build();
			companyRepository.save(testCompany);
		}

		// When
		PageCompanyResponseDto result1 = companyService.searchCompanies(q, category, page, size, sort, order);
		PageCompanyResponseDto result2 = companyService.searchCompanies(q, category, page, size, sort, order);

		// Then
		// 첫 번째 결과는 null이 아니어야 한다.
		assertNotNull(result1);

		// 두 번째 호출에서는 캐시된 결과이므로, 첫 번째 결과와 동일한지 비교
		assertNotNull(result2);
		// 결과의 내용이 동일한지 순서대로 비교
		assertEquals(result1.getContent().size(), result2.getContent().size());
		for (int i = 0; i < result1.getContent().size(); i++) {
			CompanyResDto company1 = result1.getContent().get(i);
			CompanyResDto company2 = result2.getContent().get(i);
			assertEquals(company1.getId(), company2.getId());
			assertEquals(company1.getName(), company2.getName());
			assertEquals(company1.getAddress(), company2.getAddress());
		}

		// 페이징 처리 테스트
		assertEquals(10, result1.getContent().size());
		assertEquals("Company9", result1.getContent().get(0).getName());
	}

	@Test
	@DisplayName("company 업데이트 - Success")
	void testUpdateCompanySuccess() {
		// Given
		CompanyReqDto companyReqDto = new CompanyReqDto(
			"New Name",
			UUID.randomUUID(),
			UUID.fromString("11111111-1111-1111-1111-111111111102"),
			"newSupplierAddress",
			CompanyTypes.SUPPLIER
		);

		// When
		CompanyResDto updatedCompany = companyService.updateCompany(testSupplierCompanyId, companyReqDto,
			UUID.randomUUID());

		// Then
		assertNotNull(updatedCompany);
		assertEquals("New Name", updatedCompany.getName());
		assertEquals("newSupplierAddress", updatedCompany.getAddress());
		assertEquals(CompanyTypes.SUPPLIER, updatedCompany.getType());
		assertEquals(UUID.fromString("11111111-1111-1111-1111-111111111102"), updatedCompany.getHubId());
	}

	@Test
	@DisplayName("company 업데이트 - Fail - CompanyForFound")
	void testUpdateCompanyFailCompanyNotFound() {
		// Given
		UUID failCompanyId = UUID.randomUUID();

		CompanyReqDto companyReqDto = new CompanyReqDto(
			"New Name",
			UUID.randomUUID(),
			UUID.fromString("11111111-1111-1111-1111-111111111102"),
			"newSupplierAddress",
			CompanyTypes.SUPPLIER
		);

		// When & Then
		CompanyNotFoundException exception = assertThrows(CompanyNotFoundException.class, () -> {
			companyService.updateCompany(failCompanyId, companyReqDto, UUID.randomUUID());
		});

		assertEquals("Company Not Found By  Id : " + failCompanyId, exception.getMessage());
	}

	@Test
	@DisplayName("company 업데이트 - Fail - HubForFound")
	void testUpdateCompanyFailHubFotFound() {
		// Given
		UUID failHubId = UUID.randomUUID();

		CompanyReqDto companyReqDto = new CompanyReqDto(
			"New Name",
			UUID.randomUUID(),
			failHubId,
			"newSupplierAddress",
			CompanyTypes.SUPPLIER
		);

		// When & Then
		EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
			companyService.updateCompany(testSupplierCompanyId, companyReqDto, UUID.randomUUID());
		});

		assertEquals("Hub Not Found By Id : " + failHubId, exception.getMessage());
	}

	@Test
	@DisplayName("company 삭제 - Success")
	void testDeleteCompanySuccess() {
		// When
		companyService.deleteCompany(testSupplierCompanyId, UUID.randomUUID());

		// Then
		Optional<Company> company = companyRepository.findByIdAndDeletedAtIsNull(testSupplierCompanyId);
		assertTrue(company.isEmpty());
	}

	@Test
	@DisplayName("company 삭제 - Fail - CompanyFotFound")
	void testDeleteCompanyFailCompanyFotFound() {
		// Given
		UUID failCompanyId = UUID.randomUUID();

		// When & Then
		CompanyNotFoundException exception = assertThrows(CompanyNotFoundException.class, () -> {
			companyService.deleteCompany(failCompanyId, UUID.randomUUID());
		});

		assertEquals("Company Not Found By  Id : " + failCompanyId, exception.getMessage());
	}

	@Test
	@DisplayName("company 공급업체 허브 id 반환 - Success")
	void testFindSupplierHubIdByCompanyIdSuccess() {
		// When & Then
		UUID hubId = companyService.findSupplierHubIdByCompanyId(testSupplierCompanyId);

		assertNotNull(hubId);
	}

	@Test
	@DisplayName("company 공급업체 허브 id 반환 - Fail")
	void testFindSupplierHubIdByCompanyIdFail() {
		// When & Then
		UUID hubId = companyService.findSupplierHubIdByCompanyId(testRecipientsCompanyHubId);

		assertNull(hubId);
	}

	@Test
	@DisplayName("company 수령업체 주소 반환 - Success")
	void testFindRecipientAddressByCompanyIdSuccess() {
		// When & Then
		String address = companyService.findRecipientAddressByCompanyId(testRecipientsCompanyId);

		assertNotNull(address);
	}

	@Test
	@DisplayName("company 수령업체 주소 반환 - Fail")
	void testFindRecipientAddressByCompanyIdFail() {
		// When & Then
		String address = companyService.findRecipientAddressByCompanyId(testSupplierCompanyId);

		assertNull(address);
	}

	@Test
	@DisplayName("company 수령업체 주소 반환 - Success")
	void testFindShippingCompanyInfoSuccess() {
		// When & Then
		ShippingCompanyInfoDto shippingCompanyInfoDto = companyService.findShippingCompanyInfo(testRecipientsCompanyId);

		assertNotNull(shippingCompanyInfoDto);
		assertEquals("11111111-1111-1111-1111-111111111101", shippingCompanyInfoDto.getHubId().toString());
	}

	@Test
	@DisplayName("company 수령업체 주소 반환 - Fail")
	void testFindShippingCompanyInfoFail() {
		// When & Then
		ShippingCompanyInfoDto shippingCompanyInfoDto = companyService.findShippingCompanyInfo(UUID.randomUUID());

		assertNull(shippingCompanyInfoDto);

	}

}

