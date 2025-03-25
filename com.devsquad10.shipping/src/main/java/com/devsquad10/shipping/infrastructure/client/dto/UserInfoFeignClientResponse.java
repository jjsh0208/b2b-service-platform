package com.devsquad10.shipping.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UserInfoFeignClientResponse {
	@JsonProperty("username")
	private String username;
	@JsonProperty("slackId")
	private String slackId;
}
