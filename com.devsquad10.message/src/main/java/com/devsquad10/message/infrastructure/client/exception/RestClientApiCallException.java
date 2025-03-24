package com.devsquad10.message.infrastructure.client.exception;

public class RestClientApiCallException extends RuntimeException {
	public RestClientApiCallException(String message) {
		super(message);
	}
}
