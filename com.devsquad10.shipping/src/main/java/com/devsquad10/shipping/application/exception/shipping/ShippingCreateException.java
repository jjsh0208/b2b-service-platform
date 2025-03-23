package com.devsquad10.shipping.application.exception.shipping;

public class ShippingCreateException extends RuntimeException {
	public ShippingCreateException(String message) {
		super(message);
	}
	public ShippingCreateException(String message, Throwable cause) {
		super(message, cause);
	}
}
