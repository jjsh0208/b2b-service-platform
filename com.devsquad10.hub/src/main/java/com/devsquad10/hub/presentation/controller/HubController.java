package com.devsquad10.hub.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devsquad10.hub.application.dto.req.HubCreateRequestDto;
import com.devsquad10.hub.application.dto.req.HubSearchRequestDto;
import com.devsquad10.hub.application.dto.req.HubUpdateRequestDto;
import com.devsquad10.hub.application.dto.res.ApiResponse;
import com.devsquad10.hub.application.dto.res.HubCreateResponseDto;
import com.devsquad10.hub.application.dto.res.HubGetOneResponseDto;
import com.devsquad10.hub.application.dto.res.HubUpdateResponseDto;
import com.devsquad10.hub.application.dto.res.PagedHubResponseDto;
import com.devsquad10.hub.application.service.HubRouteService;
import com.devsquad10.hub.application.service.HubService;
import com.devsquad10.hub.infrastructure.client.dto.HubFeignClientGetRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hub")
public class HubController {

	private final HubService hubService;
	private final HubRouteService hubRouteService;

	@PostMapping
	public ResponseEntity<ApiResponse<HubCreateResponseDto>> createHub(
		@Valid @RequestBody HubCreateRequestDto request
	) {
		HubCreateResponseDto response = hubService.createHub(request);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<HubGetOneResponseDto>> getHub(
		@PathVariable UUID id
	) {
		HubGetOneResponseDto response = hubService.getOneHub(id);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ApiResponse<HubUpdateResponseDto>> updateHub(
		@PathVariable UUID id,
		@Valid @RequestBody HubUpdateRequestDto request
	) {
		HubUpdateResponseDto response = hubService.updateHub(id, request);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<String>> deleteHub(
		@PathVariable UUID id
	) {
		hubService.deleteHub(id);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				"Hub successfully deleted"
			));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PagedHubResponseDto>> getAllHubs(
		@ModelAttribute @Valid HubSearchRequestDto request
	) {
		PagedHubResponseDto response = hubService.getHub(request);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	@GetMapping("/exists/{uuid}")
	public Boolean isHubExists(@PathVariable(name = "uuid") UUID uuid) {
		return hubService.existById(uuid);
	}

	@GetMapping("/info/{departureHubId}/{destinationHubId}")
	public List<HubFeignClientGetRequest> getHubRouteInfo(
		@PathVariable("departureHubId") UUID departureHubId,
		@PathVariable("destinationHubId") UUID destinationHubId
	) {

		return hubRouteService.getHubRouteInfo(
			departureHubId, destinationHubId);
	}
}
