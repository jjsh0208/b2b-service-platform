package com.devsquad10.hub.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.devsquad10.hub.infrastructure.client.exception.RestClientApiCallException;
import com.devsquad10.hub.infrastructure.client.exception.RestClientApiServerErrorException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RestClientConfig {
	@Bean
	public RestClient restClient() {
		return RestClient.builder()
			.defaultHeaders(headers -> {
				headers.setContentType(MediaType.APPLICATION_JSON);
			})
			.requestFactory(getClientHttpRequestFactory())
			.defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
				log.error("4xx Error - URL: {}, Status: {}, Body: {}",
					req.getURI(), res.getStatusCode(), res.getBody());
				throw new RestClientApiCallException(res.getStatusText());
			})
			.defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
				log.error("5xx Error - URL: {}, Status: {}, Body: {}",
					req.getURI(), res.getStatusCode(), res.getBody());
				throw new RestClientApiServerErrorException(res.getStatusText());
			})
			.build();
	}

	private ClientHttpRequestFactory getClientHttpRequestFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(10000);
		factory.setReadTimeout(10000);

		return factory;
	}
}
