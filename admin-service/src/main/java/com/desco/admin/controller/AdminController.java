package com.desco.admin.controller;

import com.desco.admin.dto.request.CreateOutageRequest;
import com.desco.admin.dto.request.UpdateComplaintRequest;
import com.desco.admin.dto.request.UpdateOutageStatusRequest;
import com.desco.admin.dto.request.UpdateUserStatusRequest;
import com.desco.admin.dto.response.*;
import com.desco.admin.entity.User;
import com.desco.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative dashboard and management endpoints (ADMIN role required)")
public class AdminController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdminService adminService;

    // --- dashboard -----------------------------------------------------------

    @GetMapping("/dashboard")
    @Operation(summary = "Aggregate dashboard across users, payments, complaints and outages")
    public ResponseEntity<DashboardResponse> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/services/health")
    @Operation(summary = "Live reachability of every sibling microservice")
    public ResponseEntity<List<ServiceStatus>> serviceHealth() {
        return ResponseEntity.ok(adminService.getServiceStatuses());
    }

    // --- users ---------------------------------------------------------------

    @GetMapping("/users")
    @Operation(summary = "List users, optionally filtered by active state")
    public ResponseEntity<PageResponse<UserResponse>> listUsers(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listUsers(isActive, pageable(page, size)));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Fetch a single user")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getUser(id));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Activate or deactivate a user account")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, request));
    }

    @GetMapping("/users/{id}/payments")
    @Operation(summary = "Payment history for a specific user")
    public ResponseEntity<List<PaymentResponse>> userPayments(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getUserPayments(id));
    }

    // --- complaints ----------------------------------------------------------

    @GetMapping("/complaints")
    @Operation(summary = "List complaints, optionally filtered by status and/or area")
    public ResponseEntity<PageResponse<ComplaintResponse>> listComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String area,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listComplaints(status, area, pageable(page, size)));
    }

    @GetMapping("/complaints/{id}")
    @Operation(summary = "Fetch a single complaint")
    public ResponseEntity<ComplaintResponse> getComplaint(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getComplaint(id));
    }

    @PatchMapping("/complaints/{id}")
    @Operation(summary = "Update a complaint's status and admin remark")
    public ResponseEntity<ComplaintResponse> updateComplaint(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateComplaintRequest request) {
        return ResponseEntity.ok(adminService.updateComplaint(id, request));
    }

    // --- outages -------------------------------------------------------------

    @GetMapping("/outages")
    @Operation(summary = "List outages, optionally filtered by status and/or area")
    public ResponseEntity<PageResponse<OutageResponse>> listOutages(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String area,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listOutages(status, area, pageable(page, size)));
    }

    @GetMapping("/outages/{id}")
    @Operation(summary = "Fetch a single outage")
    public ResponseEntity<OutageResponse> getOutage(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getOutage(id));
    }

    @PostMapping("/outages")
    @Operation(summary = "Schedule a new planned or emergency outage")
    public ResponseEntity<OutageResponse> createOutage(
            @Valid @RequestBody CreateOutageRequest request,
            @AuthenticationPrincipal User admin) {
        return new ResponseEntity<>(
                adminService.createOutage(request, admin.getId()), HttpStatus.CREATED);
    }

    @PatchMapping("/outages/{id}/status")
    @Operation(summary = "Update an outage's status")
    public ResponseEntity<OutageResponse> updateOutageStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOutageStatusRequest request) {
        return ResponseEntity.ok(adminService.updateOutageStatus(id, request));
    }

    // --- payments ------------------------------------------------------------

    @GetMapping("/payments")
    @Operation(summary = "List all payments across every user, newest first")
    public ResponseEntity<PageResponse<PaymentResponse>> listPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listPayments(pageable(page, size)));
    }

    /** Clamps paging input so a caller cannot request an unbounded result set. */
    private Pageable pageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
    }
}
