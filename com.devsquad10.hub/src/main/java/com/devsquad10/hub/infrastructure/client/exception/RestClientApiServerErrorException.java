package com.devsquad10.hub.infrastructure.client.exception;

public class RestClientApiServerErrorException extends RuntimeException {
	public RestClientApiServerErrorException(String message) {
		super(message);
	}
}
