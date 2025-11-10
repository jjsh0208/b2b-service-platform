package com.devsquad10.company.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.devsquad10.company.domain.model.Company;

public interface CompanyRepository {

	Optional<Company> findByIdAndDeletedAtIsNull(UUID id);

	Company save(Company company);

}
