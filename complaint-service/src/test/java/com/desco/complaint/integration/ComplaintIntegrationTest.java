package com.desco.complaint.integration;

import com.desco.complaint.client.NotificationClient;
import com.desco.complaint.dto.request.ComplaintRequest;
import com.desco.complaint.dto.request.ComplaintStatusUpdateRequest;
import com.desco.complaint.enums.Area;
import com.desco.complaint.enums.ComplaintCategory;
import com.desco.complaint.enums.ComplaintStatus;
import com.desco.complaint.repository.ComplaintRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class ComplaintIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ComplaintRepository complaintRepository;

    @MockBean
    private NotificationClient notificationClient;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "user@desco.org", roles = {"USER"})
    @DisplayName("Complete Complaint Lifecycle: Submit -> Query by User -> Update Status -> Delete")
    void testComplaintLifecycle() throws Exception {
        UUID userId = UUID.randomUUID();

        ComplaintRequest request = ComplaintRequest.builder()
                .userId(userId)
                .area(Area.UTTARA)
                .category(ComplaintCategory.METER_FAULT)
                .title("Smart meter display blank")
                .description("Digital reading display is unreadable")
                .build();

        // 1. Submit Complaint
        String responseContent = mockMvc.perform(post("/api/complaints")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Smart meter display blank"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        String complaintId = objectMapper.readTree(responseContent).get("data").get("id").asText();

        // 2. Query by User ID
        mockMvc.perform(get("/api/complaints/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(complaintId));

        // 3. Update Status
        ComplaintStatusUpdateRequest statusRequest = new ComplaintStatusUpdateRequest(ComplaintStatus.IN_PROGRESS, "Technician assigned");
        mockMvc.perform(patch("/api/complaints/{id}/status", complaintId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.resolutionNotes").value("Technician assigned"));

        // 4. Delete Complaint
        mockMvc.perform(delete("/api/complaints/{id}", complaintId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Verify 404
        mockMvc.perform(get("/api/complaints/{id}", complaintId))
                .andExpect(status().isNotFound());
    }
}
