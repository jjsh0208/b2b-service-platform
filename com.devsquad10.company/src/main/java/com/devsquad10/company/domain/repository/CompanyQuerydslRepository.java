package com.devsquad10.company.domain.repository;

import org.springframework.data.domain.Page;

import com.devsquad10.company.domain.model.Company;

public interface CompanyQuerydslRepository {
	Page<Company> findAll(String q, String category, int page, int size, String sort, String order);
}
