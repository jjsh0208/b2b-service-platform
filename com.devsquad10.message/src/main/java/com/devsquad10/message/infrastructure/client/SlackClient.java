package com.devsquad10.message.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SlackClient {

	@Value("${slack.api.token}")
	private String token;

	@Value("${slack.api.url}")
	private String apiUrl;
}
