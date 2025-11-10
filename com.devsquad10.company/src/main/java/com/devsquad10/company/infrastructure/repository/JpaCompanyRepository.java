package com.devsquad10.company.infrastructure.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devsquad10.company.domain.model.Company;
import com.devsquad10.company.domain.repository.CompanyRepository;

@Repository
public interface JpaCompanyRepository
	extends JpaRepository<Company, UUID>, CompanyRepository {

}
