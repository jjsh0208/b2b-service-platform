package com.devsquad10.shipping.application.exception.shipping;

public class InvalidShippingStatusUpdateException extends RuntimeException {
	public InvalidShippingStatusUpdateException(String message) {
		super(message);
	}
}
