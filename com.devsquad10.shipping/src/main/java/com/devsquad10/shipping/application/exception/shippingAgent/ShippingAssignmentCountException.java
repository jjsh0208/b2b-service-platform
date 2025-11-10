package com.devsquad10.shipping.application.exception.shippingAgent;

public class ShippingAssignmentCountException extends RuntimeException {
	public ShippingAssignmentCountException(String message) {
		super(message);
	}

	public ShippingAssignmentCountException(String message, Throwable cause) {
		super(message, cause);
	}
}
