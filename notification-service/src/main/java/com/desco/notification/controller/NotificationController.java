package com.desco.notification.controller;

import com.desco.notification.dto.request.NotificationRequest;
import com.desco.notification.dto.response.ApiResponse;
import com.desco.notification.dto.response.NotificationResponse;
import com.desco.notification.enums.Area;
import com.desco.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app alerts and announcement broadcasting APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Publish a notification alert (system / admin)")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return new ResponseEntity<>(ApiResponse.success("Notification published successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllNotifications() {
        List<NotificationResponse> list = notificationService.getAllNotifications();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(@PathVariable UUID id) {
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notification feed for a user (combining direct and area broadcasts)")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(
            @PathVariable UUID userId,
            @RequestParam(required = false) Area area) {
        List<NotificationResponse> list = notificationService.getUserNotifications(userId, area);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/area/{area}")
    @Operation(summary = "Get notification broadcasts for a specific area")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAreaNotifications(@PathVariable Area area) {
        List<NotificationResponse> list = notificationService.getAreaNotifications(area);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get total unread notifications count for a user")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(@PathVariable UUID userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a single notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @PatchMapping("/user/{userId}/read-all")
    @Operation(summary = "Mark all notifications as read for a user")
    public ResponseEntity<ApiResponse<Void>> markAllAsReadForUser(@PathVariable UUID userId) {
        notificationService.markAllAsReadForUser(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }
}
