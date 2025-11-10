package com.devsquad10.shipping.application.exception.shippingAgent;

public class ShippingAgentAlreadyAllocatedException extends RuntimeException {
	public ShippingAgentAlreadyAllocatedException(String message) {
		super(message);
	}
}
