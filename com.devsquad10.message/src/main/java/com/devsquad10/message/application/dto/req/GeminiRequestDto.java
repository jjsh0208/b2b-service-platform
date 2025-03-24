package com.devsquad10.message.application.dto.req;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeminiRequestDto {

	private List<Content> contents;

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Content {
		private List<Part> parts;

		public static Content from(String text) {
			return Content.builder()
				.parts(List.of(Part.from(text)))
				.build();
		}
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Part {
		private String text;

		public static Part from(String text) {
			return Part.builder()
				.text(text)
				.build();
		}
	}

	public static GeminiRequestDto from(String text) {
		return GeminiRequestDto.builder()
			.contents(List.of(Content.from(text)))
			.build();
	}
}
