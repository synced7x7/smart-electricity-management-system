package com.desco.complaint.controller;

import com.desco.complaint.dto.request.ComplaintRequest;
import com.desco.complaint.dto.request.ComplaintStatusUpdateRequest;
import com.desco.complaint.dto.request.ComplaintUpdateRequest;
import com.desco.complaint.dto.response.ApiResponse;
import com.desco.complaint.dto.response.ComplaintResponse;
import com.desco.complaint.enums.Area;
import com.desco.complaint.enums.ComplaintStatus;
import com.desco.complaint.service.ComplaintService;
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
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints", description = "Customer complaint submission and status management APIs")
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    @Operation(summary = "Submit a new customer complaint")
    public ResponseEntity<ApiResponse<ComplaintResponse>> submitComplaint(@Valid @RequestBody ComplaintRequest request) {
        ComplaintResponse response = complaintService.submitComplaint(request);
        return new ResponseEntity<>(ApiResponse.success("Complaint submitted successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all complaints or filter by status")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getAllComplaints(
            @RequestParam(required = false) ComplaintStatus status) {
        List<ComplaintResponse> complaints = status != null
                ? complaintService.getComplaintsByStatus(status)
                : complaintService.getAllComplaints();
        return ResponseEntity.ok(ApiResponse.success(complaints));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get complaint details by ID")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(@PathVariable UUID id) {
        ComplaintResponse response = complaintService.getComplaintById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all complaints submitted by a specific user")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getComplaintsByUser(@PathVariable UUID userId) {
        List<ComplaintResponse> complaints = complaintService.getComplaintsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(complaints));
    }

    @GetMapping("/area/{area}")
    @Operation(summary = "Filter complaints by service area")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getComplaintsByArea(@PathVariable Area area) {
        List<ComplaintResponse> complaints = complaintService.getComplaintsByArea(area);
        return ResponseEntity.ok(ApiResponse.success(complaints));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update complaint details")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaint(
            @PathVariable UUID id,
            @Valid @RequestBody ComplaintUpdateRequest request) {
        ComplaintResponse response = complaintService.updateComplaint(id, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update complaint resolution status (Admin/Staff)")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaintStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ComplaintStatusUpdateRequest request) {
        ComplaintResponse response = complaintService.updateComplaintStatus(id, request.getStatus(), request.getResolutionNotes());
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a complaint record")
    public ResponseEntity<ApiResponse<Void>> deleteComplaint(@PathVariable UUID id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.ok(ApiResponse.success("Complaint deleted successfully", null));
    }
}
