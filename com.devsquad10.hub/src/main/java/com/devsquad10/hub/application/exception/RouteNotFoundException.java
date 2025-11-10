package com.devsquad10.hub.application.exception;

public class RouteNotFoundException extends RuntimeException {
	public RouteNotFoundException(String message) {
		super(message);
	}
}
