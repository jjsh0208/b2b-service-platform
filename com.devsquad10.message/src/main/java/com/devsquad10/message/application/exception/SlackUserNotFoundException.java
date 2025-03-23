package com.devsquad10.message.application.exception;

public class SlackUserNotFoundException extends RuntimeException {
	public SlackUserNotFoundException(String message) {
		super(message);
	}
}
