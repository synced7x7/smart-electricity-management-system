package com.desco.notification.integration;

import com.desco.notification.dto.request.NotificationRequest;
import com.desco.notification.enums.Area;
import com.desco.notification.enums.NotificationType;
import com.desco.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@desco.org", roles = {"ADMIN"})
    @DisplayName("Complete Notification Lifecycle: Create -> Get User Feed -> Mark Read -> Unread Count -> Delete")
    void testNotificationLifecycle() throws Exception {
        UUID userId = UUID.randomUUID();

        NotificationRequest request = NotificationRequest.builder()
                .targetUserId(userId)
                .targetArea(Area.BASHUNDHARA)
                .title("Load Shedding Warning")
                .message("High grid load expected from 7 PM to 9 PM")
                .type(NotificationType.OUTAGE_ALERT)
                .build();

        // 1. Create Notification
        String responseContent = mockMvc.perform(post("/api/notifications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Load Shedding Warning"))
                .andExpect(jsonPath("$.data.isRead").value(false))
                .andReturn().getResponse().getContentAsString();

        String notificationId = objectMapper.readTree(responseContent).get("data").get("id").asText();

        // 2. Check Unread Count = 1
        mockMvc.perform(get("/api/notifications/user/{userId}/unread-count", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(1));

        // 3. Get User Notification Feed
        mockMvc.perform(get("/api/notifications/user/{userId}?area=BASHUNDHARA", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(notificationId));

        // 4. Mark as Read
        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isRead").value(true));

        // 5. Check Unread Count = 0
        mockMvc.perform(get("/api/notifications/user/{userId}/unread-count", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(0));

        // 6. Delete Notification
        mockMvc.perform(delete("/api/notifications/{id}", notificationId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 7. Verify Not Found
        mockMvc.perform(get("/api/notifications/{id}", notificationId))
                .andExpect(status().isNotFound());
    }
}
