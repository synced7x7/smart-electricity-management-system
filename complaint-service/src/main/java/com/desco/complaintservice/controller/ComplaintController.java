package com.desco.complaintservice.controller;

import com.desco.complaintservice.dto.request.CreateComplaintRequest;
import com.desco.complaintservice.dto.request.UpdateComplaintRequest;
import com.desco.complaintservice.dto.request.UpdateComplaintStatusRequest;
import com.desco.complaintservice.dto.response.ApiResponse;
import com.desco.complaintservice.dto.response.ComplaintResponse;
import com.desco.complaintservice.enums.Area;
import com.desco.complaintservice.enums.ComplaintStatus;
import com.desco.complaintservice.security.UserPrincipal;
import com.desco.complaintservice.service.ComplaintService;
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
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaint", description = "Endpoints for submitting and managing user complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    @GetMapping("/ping")
    @Operation(summary = "Health ping endpoint")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("service", "complaint-service", "status", "ok"));
    }

    // ── User Endpoints ────────────────────────────────────────────────────────

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Submit a new complaint")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Complaint submitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CreateComplaintRequest request) {

        log.info("POST /api/complaints — userId={}", user.getId());
        ComplaintResponse response = complaintService.createComplaint(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint submitted successfully", response));
    }

    @GetMapping("/my")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all complaints submitted by the current user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaints retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> getMyComplaints(
            @AuthenticationPrincipal UserPrincipal user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/complaints/my — userId={}", user.getId());
        Page<ComplaintResponse> page = complaintService.getMyComplaints(user, pageable);
        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved successfully", page));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get complaint by ID (owner or ADMIN)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Complaint not found")
    })
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user) {

        log.info("GET /api/complaints/{} — requesterId={}", id, user.getId());
        ComplaintResponse response = complaintService.getComplaintById(id, user);
        return ResponseEntity.ok(ApiResponse.success("Complaint retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a complaint (owner only, while SUBMITTED)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Complaint not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Complaint is no longer editable")
    })
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaint(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody UpdateComplaintRequest request) {

        log.info("PUT /api/complaints/{} — userId={}", id, user.getId());
        ComplaintResponse response = complaintService.updateComplaint(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Complaint updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a complaint (owner while SUBMITTED, or ADMIN)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Complaint not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Complaint cannot be deleted in current status")
    })
    public ResponseEntity<ApiResponse<Void>> deleteComplaint(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user) {

        log.info("DELETE /api/complaints/{} — requesterId={}", id, user.getId());
        complaintService.deleteComplaint(id, user);
        return ResponseEntity.ok(ApiResponse.success("Complaint deleted successfully"));
    }

    // ── Admin Endpoints ───────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all complaints paginated (ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All complaints retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> getAllComplaints(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/complaints — admin access");
        Page<ComplaintResponse> page = complaintService.getAllComplaints(pageable);
        return ResponseEntity.ok(ApiResponse.success("All complaints retrieved successfully", page));
    }

    @GetMapping("/area/{area}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get complaints filtered by area (ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaints retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> getComplaintsByArea(
            @PathVariable Area area,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/complaints/area/{} — admin access", area);
        Page<ComplaintResponse> page = complaintService.getComplaintsByArea(area, pageable);
        return ResponseEntity.ok(ApiResponse.success("Complaints for area " + area + " retrieved successfully", page));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get complaints filtered by status (ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaints retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> getComplaintsByStatus(
            @PathVariable ComplaintStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/complaints/status/{} — admin access", status);
        Page<ComplaintResponse> page = complaintService.getComplaintsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Complaints with status " + status + " retrieved successfully", page));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Search complaints by subject or description (ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> searchComplaints(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String searchTerm = (query != null && !query.isBlank()) ? query : q;
        log.info("GET /api/complaints/search?query={} — admin access", searchTerm);
        Page<ComplaintResponse> page = complaintService.searchComplaints(searchTerm, pageable);
        return ResponseEntity.ok(ApiResponse.success("Complaint search results retrieved", page));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update complaint status (ADMIN only) — triggers user notification")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Complaint not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaintStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateComplaintStatusRequest request) {

        log.info("PATCH /api/complaints/{}/status — newStatus={}", id, request.getStatus());
        ComplaintResponse response = complaintService.updateComplaintStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
    }
}
