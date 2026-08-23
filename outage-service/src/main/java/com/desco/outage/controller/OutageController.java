package com.desco.outage.controller;

import com.desco.outage.dto.request.OutageRequest;
import com.desco.outage.dto.request.OutageStatusUpdateRequest;
import com.desco.outage.dto.response.ApiResponse;
import com.desco.outage.dto.response.OutageResponse;
import com.desco.outage.enums.Area;
import com.desco.outage.enums.OutageStatus;
import com.desco.outage.service.OutageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/outages")
@RequiredArgsConstructor
@Tag(name = "Outages", description = "Power outage schedules and management APIs")
public class OutageController {

    private final OutageService outageService;

    @PostMapping
    @Operation(summary = "Create a scheduled or emergency outage record")
    public ResponseEntity<ApiResponse<OutageResponse>> createOutage(@Valid @RequestBody OutageRequest request) {
        OutageResponse response = outageService.createOutage(request);
        return new ResponseEntity<>(ApiResponse.success("Outage scheduled successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all outages or filter by status")
    public ResponseEntity<ApiResponse<List<OutageResponse>>> getAllOutages(
            @RequestParam(required = false) OutageStatus status) {
        List<OutageResponse> outages = status != null
                ? outageService.getOutagesByStatus(status)
                : outageService.getAllOutages();
        return ResponseEntity.ok(ApiResponse.success(outages));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get outage details by ID")
    public ResponseEntity<ApiResponse<OutageResponse>> getOutageById(@PathVariable UUID id) {
        OutageResponse response = outageService.getOutageById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/area/{area}")
    @Operation(summary = "Filter outages by geographic service area")
    public ResponseEntity<ApiResponse<List<OutageResponse>>> getOutagesByArea(@PathVariable Area area) {
        List<OutageResponse> outages = outageService.getOutagesByArea(area);
        return ResponseEntity.ok(ApiResponse.success(outages));
    }

    @GetMapping("/active")
    @Operation(summary = "List currently active and scheduled outages")
    public ResponseEntity<ApiResponse<List<OutageResponse>>> getActiveOutages() {
        List<OutageResponse> outages = outageService.getActiveOutages();
        return ResponseEntity.ok(ApiResponse.success(outages));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing outage schedule")
    public ResponseEntity<ApiResponse<OutageResponse>> updateOutage(
            @PathVariable UUID id,
            @Valid @RequestBody OutageRequest request) {
        OutageResponse response = outageService.updateOutage(id, request);
        return ResponseEntity.ok(ApiResponse.success("Outage updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update status of an outage (e.g., SCHEDULED -> ONGOING -> RESOLVED)")
    public ResponseEntity<ApiResponse<OutageResponse>> updateOutageStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OutageStatusUpdateRequest request) {
        OutageResponse response = outageService.updateOutageStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Outage status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an outage record")
    public ResponseEntity<ApiResponse<Void>> deleteOutage(@PathVariable UUID id) {
        outageService.deleteOutage(id);
        return ResponseEntity.ok(ApiResponse.success("Outage deleted successfully", null));
    }
}
