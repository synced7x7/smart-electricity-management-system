package com.desco.complaint.controller;

import com.desco.complaint.dto.request.ComplaintRequest;
import com.desco.complaint.dto.request.ComplaintStatusUpdateRequest;
import com.desco.complaint.dto.request.ComplaintUpdateRequest;
import com.desco.complaint.dto.response.ComplaintResponse;
import com.desco.complaint.enums.Area;
import com.desco.complaint.enums.ComplaintCategory;
import com.desco.complaint.enums.ComplaintStatus;
import com.desco.complaint.exception.GlobalExceptionHandler;
import com.desco.complaint.exception.ResourceNotFoundException;
import com.desco.complaint.security.JwtAuthFilter;
import com.desco.complaint.security.JwtService;
import com.desco.complaint.service.ComplaintService;
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

@WebMvcTest(controllers = {ComplaintController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComplaintService complaintService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private UUID complaintId;
    private UUID userId;
    private ComplaintResponse sampleResponse;
    private ComplaintRequest sampleRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        complaintId = UUID.randomUUID();
        userId = UUID.randomUUID();
        now = LocalDateTime.now();

        sampleResponse = ComplaintResponse.builder()
                .id(complaintId)
                .userId(userId)
                .area(Area.MIRPUR)
                .category(ComplaintCategory.POWER_CUT)
                .title("Unscheduled power outage")
                .description("No power for 3 hours in section 10")
                .status(ComplaintStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        sampleRequest = ComplaintRequest.builder()
                .userId(userId)
                .area(Area.MIRPUR)
                .category(ComplaintCategory.POWER_CUT)
                .title("Unscheduled power outage")
                .description("No power for 3 hours in section 10")
                .build();
    }

    @Test
    @DisplayName("POST /api/complaints submits complaint successfully")
    void testSubmitComplaint() throws Exception {
        when(complaintService.submitComplaint(any(ComplaintRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Unscheduled power outage"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/complaints returns list of complaints")
    void testGetAllComplaints() throws Exception {
        when(complaintService.getAllComplaints()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(complaintId.toString()));
    }

    @Test
    @DisplayName("GET /api/complaints/{id} returns single complaint")
    void testGetComplaintById() throws Exception {
        when(complaintService.getComplaintById(complaintId)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/complaints/{id}", complaintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Unscheduled power outage"));
    }

    @Test
    @DisplayName("GET /api/complaints/{id} returns 404 when missing")
    void testGetComplaintById_NotFound() throws Exception {
        when(complaintService.getComplaintById(complaintId))
                .thenThrow(new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        mockMvc.perform(get("/api/complaints/{id}", complaintId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    @DisplayName("GET /api/complaints/user/{userId} returns user's complaints")
    void testGetComplaintsByUser() throws Exception {
        when(complaintService.getComplaintsByUser(userId)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/complaints/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value(userId.toString()));
    }

    @Test
    @DisplayName("PATCH /api/complaints/{id}/status updates status")
    void testUpdateComplaintStatus() throws Exception {
        ComplaintStatusUpdateRequest statusRequest = new ComplaintStatusUpdateRequest(ComplaintStatus.IN_PROGRESS, "Team dispatched");
        sampleResponse.setStatus(ComplaintStatus.IN_PROGRESS);
        sampleResponse.setResolutionNotes("Team dispatched");

        when(complaintService.updateComplaintStatus(eq(complaintId), eq(ComplaintStatus.IN_PROGRESS), eq("Team dispatched")))
                .thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/complaints/{id}/status", complaintId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("DELETE /api/complaints/{id} deletes complaint")
    void testDeleteComplaint() throws Exception {
        doNothing().when(complaintService).deleteComplaint(complaintId);

        mockMvc.perform(delete("/api/complaints/{id}", complaintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Complaint deleted successfully"));
    }
}
