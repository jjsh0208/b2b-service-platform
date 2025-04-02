package com.devsquad10.shipping.infrastructure.config.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI(){
		Info info = new Info()
			.version("v1.0")
			.title("Shipping Service API")
			.description("B2B LOGISTIC 배송(배송, 배송 경로 기록, 배송 담당자 관리) 관련 API");
		return new OpenAPI()
			.info(info);
	}
}
