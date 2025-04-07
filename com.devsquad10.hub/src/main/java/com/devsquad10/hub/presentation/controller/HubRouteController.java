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

import com.devsquad10.hub.application.dto.req.HubRouteCreateRequestDto;
import com.devsquad10.hub.application.dto.req.HubRouteSearchRequestDto;
import com.devsquad10.hub.application.dto.req.HubRouteUpdateRequestDto;
import com.devsquad10.hub.application.dto.res.ApiResponse;
import com.devsquad10.hub.application.dto.res.HubRouteCreateResponseDto;
import com.devsquad10.hub.application.dto.res.HubRouteGetOneResponseDto;
import com.devsquad10.hub.application.dto.res.HubRouteUpdateResponseDto;
import com.devsquad10.hub.application.dto.res.PagedHubRouteResponseDto;
import com.devsquad10.hub.application.service.HubRouteService;
import com.devsquad10.hub.infrastructure.client.dto.HubFeignClientGetRequest;
import com.devsquad10.hub.presentation.documentation.HubRouterSwaggerDocs;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hub-route")
public class HubRouteController {

	private final HubRouteService hubRouteService;

	@PostMapping
	@HubRouterSwaggerDocs.CreateHubRoute
	public ResponseEntity<ApiResponse<HubRouteCreateResponseDto>> createHubRoute(
		@Valid @RequestBody HubRouteCreateRequestDto request
	) {
		HubRouteCreateResponseDto response = hubRouteService.createHubRoute(request);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	@GetMapping("/{id}")
	@HubRouterSwaggerDocs.GetHubRoute
	public ResponseEntity<ApiResponse<HubRouteGetOneResponseDto>> getHubRoute(
		@PathVariable UUID id
	) {
		HubRouteGetOneResponseDto response = hubRouteService.getOneHubRoute(id);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	@PatchMapping("/{id}")
	@HubRouterSwaggerDocs.UpdateHubRoute
	public ResponseEntity<ApiResponse<HubRouteUpdateResponseDto>> updateHubRoute(
		@PathVariable UUID id,
		@Valid @RequestBody HubRouteUpdateRequestDto request
	) {
		HubRouteUpdateResponseDto response = hubRouteService.updateHubRoute(id, request);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	@DeleteMapping("/{id}")
	@HubRouterSwaggerDocs.DeleteHubRoute
	public ResponseEntity<ApiResponse<String>> deleteHubRoute(
		@PathVariable UUID id
	) {
		hubRouteService.deleteHubRoute(id);
		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				"HubRoute successfully deleted"
			));
	}

	@GetMapping
	@HubRouterSwaggerDocs.SearchHubRoutes
	public ResponseEntity<ApiResponse<PagedHubRouteResponseDto>> getAllHubs(
		@ModelAttribute @Valid HubRouteSearchRequestDto request
	) {
		PagedHubRouteResponseDto response = hubRouteService.getHub(request);

		return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponse.success(
				HttpStatus.OK.value(),
				response
			));
	}

	@GetMapping("/info/{departureHubId}/{destinationHubId}")
	@HubRouterSwaggerDocs.getHubRouteInfo
	public List<HubFeignClientGetRequest> getHubRouteInfo(
		@PathVariable("departureHubId") UUID departureHubId,
		@PathVariable("destinationHubId") UUID destinationHubId
	) {

		return hubRouteService.getHubRouteInfo(
			departureHubId, destinationHubId);
	}
}
