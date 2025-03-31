package com.devsquad10.hub;

import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.devsquad10.hub.application.dto.req.HubCreateRequestDto;
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class HubApiDocsTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void testHubPostSuccessWithDocs() throws Exception {

		HubCreateRequestDto requestDto = HubCreateRequestDto.builder()
			.name("테스트 센터")
			.address("경기도 성남시 분당구 정자일로 95")
			.latitude(37.3591784)
			.longitude(127.1048319)
			.build();

		String requestJson = objectMapper.writeValueAsString(requestDto);

		mockMvc.perform(
				RestDocumentationRequestBuilders.post("/api/hub")
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestJson)
					.header("Authorization", "Bearer test-token")
			)
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(
				MockMvcRestDocumentationWrapper.document("hub-create-success",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					resource(ResourceSnippetParameters.builder()
						.tag("Hub API")
						.description("허브 생성 API")
						.requestFields(
							fieldWithPath("name").description("허브 이름"),
							fieldWithPath("address").description("허브 주소"),
							fieldWithPath("latitude").description("위도"),
							fieldWithPath("longitude").description("경도")
						)
						.responseFields(
							fieldWithPath("status").description("HTTP 상태 코드"),
							fieldWithPath("success").description("성공 여부"),
							fieldWithPath("body.id").description("생성된 허브 ID"),
							fieldWithPath("body.name").description("허브 이름"),
							fieldWithPath("body.address").description("허브 주소"),
							fieldWithPath("body.latitude").description("위도"),
							fieldWithPath("body.longitude").description("경도"),
							fieldWithPath("body.createdAt").description("생성 시간"),
							fieldWithPath("body.createdBy").description("생성자 ID"),
							fieldWithPath("body.updatedAt").description("마지막 수정 시간"),
							fieldWithPath("body.updatedBy").description("수정자 ID"),
							fieldWithPath("body.deletedAt").description("삭제 시간(null이면 삭제되지 않음)").optional(),
							fieldWithPath("body.deletedBy").description("삭제자 ID(null이면 삭제되지 않음)").optional()
						)
						.build()
					)
				)
			);
	}
}
