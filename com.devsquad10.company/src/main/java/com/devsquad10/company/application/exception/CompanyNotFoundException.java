package com.devsquad10.company.application.exception;

public class CompanyNotFoundException extends RuntimeException {
	public CompanyNotFoundException(String message) {
		super(message);
	}
}
