package com.desco.outage.integration;

import com.desco.outage.client.NotificationClient;
import com.desco.outage.dto.request.OutageRequest;
import com.desco.outage.enums.Area;
import com.desco.outage.enums.OutageStatus;
import com.desco.outage.enums.OutageType;
import com.desco.outage.repository.OutageRepository;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class OutageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutageRepository outageRepository;

    @MockBean
    private NotificationClient notificationClient;

    @BeforeEach
    void setUp() {
        outageRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@desco.org", roles = {"ADMIN"})
    @DisplayName("Complete Outage Lifecycle: Create -> Get by ID -> Filter Area -> Delete")
    void testOutageLifecycle() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(3);

        OutageRequest request = OutageRequest.builder()
                .title("Gulshan Feeder Overhaul")
                .area(Area.GULSHAN)
                .outageType(OutageType.SCHEDULED)
                .status(OutageStatus.SCHEDULED)
                .reason("Replacing 33kV switchgear")
                .startTime(start)
                .endTime(end)
                .build();

        // 1. Create Outage
        String responseContent = mockMvc.perform(post("/api/outages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Gulshan Feeder Overhaul"))
                .andExpect(jsonPath("$.data.area").value("GULSHAN"))
                .andReturn().getResponse().getContentAsString();

        String outageId = objectMapper.readTree(responseContent).get("data").get("id").asText();

        // 2. Get Outage by ID
        mockMvc.perform(get("/api/outages/{id}", outageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(outageId))
                .andExpect(jsonPath("$.data.reason").value("Replacing 33kV switchgear"));

        // 3. Filter by Area
        mockMvc.perform(get("/api/outages/area/{area}", "GULSHAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].area").value("GULSHAN"));

        // 4. Delete Outage
        mockMvc.perform(delete("/api/outages/{id}", outageId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Verify Not Found
        mockMvc.perform(get("/api/outages/{id}", outageId))
                .andExpect(status().isNotFound());
    }
}
