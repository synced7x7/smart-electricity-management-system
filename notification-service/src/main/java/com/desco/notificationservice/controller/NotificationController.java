package com.desco.notificationservice.controller;

import com.desco.notificationservice.dto.request.BroadcastNotificationRequest;
import com.desco.notificationservice.dto.response.ApiResponse;
import com.desco.notificationservice.dto.response.NotificationResponse;
import com.desco.notificationservice.security.UserPrincipal;
import com.desco.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Endpoints for managing user notifications and system broadcasts")
public class NotificationController {

    private final NotificationService notificationService;

    @Value("${internal.api-key:desco-internal-secret-key-12345}")
    private String configuredInternalKey;

    @GetMapping("/ping")
    @Operation(summary = "Health ping endpoint")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("service", "notification-service", "status", "ok"));
    }

    @GetMapping("/my")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get notifications for current user (personal and area-targeted)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/notifications/my — userId={}", user.getId());
        Page<NotificationResponse> result = notificationService.getMyNotifications(user, pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", result));
    }

    @GetMapping("/unread-count")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get count of unread notifications for current user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unread count retrieved successfully")
    })
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal user) {

        log.info("GET /api/notifications/unread-count — userId={}", user.getId());
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", Map.of("unreadCount", count)));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get single notification by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user) {

        log.info("GET /api/notifications/{} — userId={}", id, user.getId());
        NotificationResponse response = notificationService.getNotificationById(id, user);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", response));
    }

    @PatchMapping("/{id}/read")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mark single notification as read")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user) {

        log.info("PATCH /api/notifications/{}/read — userId={}", id, user.getId());
        NotificationResponse response = notificationService.markAsRead(id, user);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @PatchMapping("/read-all")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mark all notifications for current user as read")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All notifications marked as read")
    })
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal user) {

        log.info("PATCH /api/notifications/read-all — userId={}", user.getId());
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a notification")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user) {

        log.info("DELETE /api/notifications/{} — userId={}", id, user.getId());
        notificationService.deleteNotification(id, user);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all notifications (ADMIN only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All notifications retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getAllNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/notifications — admin access");
        Page<NotificationResponse> page = notificationService.getAllNotifications(pageable);
        return ResponseEntity.ok(ApiResponse.success("All notifications retrieved successfully", page));
    }

    @PostMapping("/internal/broadcast")
    @Operation(summary = "Internal service-to-service notification broadcast (Course-project simplification via internal API key)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Notification broadcast created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid internal key")
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> broadcastInternal(
            @Parameter(description = "Shared internal API secret key")
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey,
            @Valid @RequestBody BroadcastNotificationRequest request) {

        log.info("POST /api/notifications/internal/broadcast — type={}, area={}, userId={}",
                request.getType(), request.getArea(), request.getUserId());

        if (internalKey == null || !configuredInternalKey.equals(internalKey)) {
            log.warn("Unauthorized internal broadcast attempt with invalid X-Internal-Key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or missing internal API key."));
        }

        NotificationResponse response = notificationService.broadcastNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification broadcasted successfully", response));
    }
}
