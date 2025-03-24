package com.devsquad10.message.application.exception;

public class SlackApiException extends RuntimeException {
	public SlackApiException(String message) {
		super(message);
	}
}
