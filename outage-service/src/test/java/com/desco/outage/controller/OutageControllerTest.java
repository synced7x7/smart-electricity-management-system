package com.desco.outage.controller;

import com.desco.outage.dto.request.OutageRequest;
import com.desco.outage.dto.request.OutageStatusUpdateRequest;
import com.desco.outage.dto.response.OutageResponse;
import com.desco.outage.enums.Area;
import com.desco.outage.enums.OutageStatus;
import com.desco.outage.enums.OutageType;
import com.desco.outage.exception.GlobalExceptionHandler;
import com.desco.outage.exception.ResourceNotFoundException;
import com.desco.outage.security.JwtAuthFilter;
import com.desco.outage.security.JwtService;
import com.desco.outage.service.OutageService;
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

@WebMvcTest(controllers = {OutageController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class OutageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OutageService outageService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private UUID outageId;
    private OutageResponse sampleResponse;
    private OutageRequest sampleRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        outageId = UUID.randomUUID();
        now = LocalDateTime.now();

        sampleResponse = OutageResponse.builder()
                .id(outageId)
                .title("Emergency Grid Repair")
                .area(Area.UTTARA)
                .outageType(OutageType.EMERGENCY)
                .status(OutageStatus.ONGOING)
                .reason("Storm line collapse")
                .startTime(now)
                .endTime(now.plusHours(2))
                .createdAt(now)
                .updatedAt(now)
                .build();

        sampleRequest = OutageRequest.builder()
                .title("Emergency Grid Repair")
                .area(Area.UTTARA)
                .outageType(OutageType.EMERGENCY)
                .status(OutageStatus.ONGOING)
                .reason("Storm line collapse")
                .startTime(now)
                .endTime(now.plusHours(2))
                .build();
    }

    @Test
    @DisplayName("POST /api/outages creates outage successfully")
    void testCreateOutage() throws Exception {
        when(outageService.createOutage(any(OutageRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/outages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Emergency Grid Repair"))
                .andExpect(jsonPath("$.data.area").value("UTTARA"));
    }

    @Test
    @DisplayName("GET /api/outages returns list of outages")
    void testGetAllOutages() throws Exception {
        when(outageService.getAllOutages()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/outages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(outageId.toString()));
    }

    @Test
    @DisplayName("GET /api/outages/{id} returns single outage")
    void testGetOutageById() throws Exception {
        when(outageService.getOutageById(outageId)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/outages/{id}", outageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Emergency Grid Repair"));
    }

    @Test
    @DisplayName("GET /api/outages/{id} returns 404 when not found")
    void testGetOutageById_NotFound() throws Exception {
        when(outageService.getOutageById(outageId))
                .thenThrow(new ResourceNotFoundException("Outage not found with id: " + outageId));

        mockMvc.perform(get("/api/outages/{id}", outageId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    @DisplayName("GET /api/outages/area/{area} filters outages by area")
    void testGetOutagesByArea() throws Exception {
        when(outageService.getOutagesByArea(Area.UTTARA)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/outages/area/{area}", "UTTARA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].area").value("UTTARA"));
    }

    @Test
    @DisplayName("PATCH /api/outages/{id}/status updates outage status")
    void testUpdateOutageStatus() throws Exception {
        OutageStatusUpdateRequest statusRequest = new OutageStatusUpdateRequest(OutageStatus.RESOLVED);
        sampleResponse.setStatus(OutageStatus.RESOLVED);
        when(outageService.updateOutageStatus(eq(outageId), eq(OutageStatus.RESOLVED))).thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/outages/{id}/status", outageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }

    @Test
    @DisplayName("DELETE /api/outages/{id} deletes outage")
    void testDeleteOutage() throws Exception {
        doNothing().when(outageService).deleteOutage(outageId);

        mockMvc.perform(delete("/api/outages/{id}", outageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Outage deleted successfully"));
    }
}
