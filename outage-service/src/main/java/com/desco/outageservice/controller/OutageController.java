package com.desco.outageservice.controller;

import com.desco.outageservice.dto.request.CreateOutageRequest;
import com.desco.outageservice.dto.request.UpdateOutageRequest;
import com.desco.outageservice.dto.request.UpdateOutageStatusRequest;
import com.desco.outageservice.dto.response.ApiResponse;
import com.desco.outageservice.dto.response.OutageResponse;
import com.desco.outageservice.enums.Area;
import com.desco.outageservice.security.UserPrincipal;
import com.desco.outageservice.service.OutageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/outages")
@RequiredArgsConstructor
@Tag(name = "Outage", description = "Endpoints for scheduled maintenance and emergency outage management")
public class OutageController {

    private final OutageService outageService;

    @GetMapping("/ping")
    @Operation(summary = "Health ping endpoint")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("service", "outage-service", "status", "ok"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create an outage (ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Outage created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<OutageResponse>> createOutage(
            @AuthenticationPrincipal UserPrincipal admin,
            @Valid @RequestBody CreateOutageRequest request) {

        log.info("POST /api/outages — adminId={}, area={}", admin.getId(), request.getArea());
        OutageResponse response = outageService.createOutage(request, admin);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Outage scheduled and notification broadcasted successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all outages paginated (ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Outages retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<OutageResponse>>> getAllOutages(
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/outages — admin access");
        Page<OutageResponse> page = outageService.getAllOutages(pageable);
        return ResponseEntity.ok(ApiResponse.success("All outages retrieved successfully", page));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get outage details by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Outage retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Outage not found")
    })
    public ResponseEntity<ApiResponse<OutageResponse>> getOutageById(@PathVariable UUID id) {
        log.info("GET /api/outages/{}", id);
        OutageResponse response = outageService.getOutageById(id);
        return ResponseEntity.ok(ApiResponse.success("Outage retrieved successfully", response));
    }

    @GetMapping("/my-area")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get outages for caller's area (active outages prioritized)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Area outages retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "User has no assigned area")
    })
    public ResponseEntity<ApiResponse<Page<OutageResponse>>> getMyAreaOutages(
            @AuthenticationPrincipal UserPrincipal user,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/outages/my-area — userId={}, area={}", user.getId(), user.getArea());
        Page<OutageResponse> page = outageService.getMyAreaOutages(user, pageable);
        return ResponseEntity.ok(ApiResponse.success("Area outages retrieved successfully", page));
    }

    @GetMapping("/area/{area}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get outages filtered by area (ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Outages retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<OutageResponse>>> getOutagesByArea(
            @PathVariable Area area,
            @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/outages/area/{} — admin access", area);
        Page<OutageResponse> page = outageService.getOutagesByArea(area, pageable);
        return ResponseEntity.ok(ApiResponse.success("Outages for area " + area + " retrieved successfully", page));
    }

    @GetMapping("/history")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get resolved and cancelled outage history")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Outage history retrieved")
    })
    public ResponseEntity<ApiResponse<Page<OutageResponse>>> getOutageHistory(
            @AuthenticationPrincipal UserPrincipal user,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("GET /api/outages/history — userId={}", user.getId());
        Page<OutageResponse> page = outageService.getOutageHistory(user, pageable);
        return ResponseEntity.ok(ApiResponse.success("Outage history retrieved successfully", page));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update outage details (ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Outage updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Outage not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<OutageResponse>> updateOutage(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOutageRequest request) {

        log.info("PUT /api/outages/{}", id);
        OutageResponse response = outageService.updateOutage(id, request);
        return ResponseEntity.ok(ApiResponse.success("Outage updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update outage status only (ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Outage status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Outage not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<OutageResponse>> updateOutageStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOutageStatusRequest request) {

        log.info("PATCH /api/outages/{}/status — newStatus={}", id, request.getStatus());
        OutageResponse response = outageService.updateOutageStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Outage status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete an outage (only if SCHEDULED and not started, ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Outage deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cannot delete started or ongoing outage"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Outage not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Void>> deleteOutage(@PathVariable UUID id) {
        log.info("DELETE /api/outages/{}", id);
        outageService.deleteOutage(id);
        return ResponseEntity.ok(ApiResponse.success("Outage deleted successfully"));
    }
}
