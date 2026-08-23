package com.desco.notification.controller;

import com.desco.notification.dto.request.NotificationRequest;
import com.desco.notification.dto.response.NotificationResponse;
import com.desco.notification.enums.Area;
import com.desco.notification.enums.NotificationType;
import com.desco.notification.exception.GlobalExceptionHandler;
import com.desco.notification.exception.ResourceNotFoundException;
import com.desco.notification.security.JwtAuthFilter;
import com.desco.notification.security.JwtService;
import com.desco.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {NotificationController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private UUID notificationId;
    private UUID userId;
    private NotificationResponse sampleResponse;
    private NotificationRequest sampleRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        notificationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        now = LocalDateTime.now();

        sampleResponse = NotificationResponse.builder()
                .id(notificationId)
                .targetUserId(userId)
                .targetArea(Area.DHANMONDI)
                .title("Maintenance Alert")
                .message("Scheduled substation check")
                .type(NotificationType.OUTAGE_ALERT)
                .isRead(false)
                .createdAt(now)
                .build();

        sampleRequest = NotificationRequest.builder()
                .targetUserId(userId)
                .targetArea(Area.DHANMONDI)
                .title("Maintenance Alert")
                .message("Scheduled substation check")
                .type(NotificationType.OUTAGE_ALERT)
                .build();
    }

    @Test
    @DisplayName("POST /api/notifications creates notification successfully")
    void testCreateNotification() throws Exception {
        when(notificationService.createNotification(any(NotificationRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Maintenance Alert"));
    }

    @Test
    @DisplayName("GET /api/notifications returns list of notifications")
    void testGetAllNotifications() throws Exception {
        when(notificationService.getAllNotifications()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(notificationId.toString()));
    }

    @Test
    @DisplayName("GET /api/notifications/{id} returns single notification")
    void testGetNotificationById() throws Exception {
        when(notificationService.getNotificationById(notificationId)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/notifications/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Maintenance Alert"));
    }

    @Test
    @DisplayName("GET /api/notifications/{id} returns 404 when missing")
    void testGetNotificationById_NotFound() throws Exception {
        when(notificationService.getNotificationById(notificationId))
                .thenThrow(new ResourceNotFoundException("Notification not found with id: " + notificationId));

        mockMvc.perform(get("/api/notifications/{id}", notificationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    @DisplayName("GET /api/notifications/user/{userId} returns user's notification feed")
    void testGetUserNotifications() throws Exception {
        when(notificationService.getUserNotifications(eq(userId), any())).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/notifications/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].targetUserId").value(userId.toString()));
    }

    @Test
    @DisplayName("GET /api/notifications/user/{userId}/unread-count returns count")
    void testGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount(userId)).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/user/{userId}/unread-count", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(3));
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read marks notification as read")
    void testMarkAsRead() throws Exception {
        sampleResponse.setIsRead(true);
        when(notificationService.markAsRead(notificationId)).thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isRead").value(true));
    }

    @Test
    @DisplayName("DELETE /api/notifications/{id} deletes notification")
    void testDeleteNotification() throws Exception {
        doNothing().when(notificationService).deleteNotification(notificationId);

        mockMvc.perform(delete("/api/notifications/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification deleted successfully"));
    }
}
