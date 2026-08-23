package com.desco.outage.service;

import com.desco.outage.client.NotificationClient;
import com.desco.outage.dto.request.OutageRequest;
import com.desco.outage.dto.response.OutageResponse;
import com.desco.outage.entity.Outage;
import com.desco.outage.enums.Area;
import com.desco.outage.enums.OutageStatus;
import com.desco.outage.enums.OutageType;
import com.desco.outage.exception.ResourceNotFoundException;
import com.desco.outage.repository.OutageRepository;
import com.desco.outage.service.impl.OutageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutageServiceTest {

    @Mock
    private OutageRepository outageRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private OutageServiceImpl outageService;

    private Outage sampleOutage;
    private OutageRequest sampleRequest;
    private UUID outageId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        outageId = UUID.randomUUID();
        now = LocalDateTime.now();

        sampleOutage = Outage.builder()
                .id(outageId)
                .title("Substation Grid Maintenance")
                .area(Area.DHANMONDI)
                .outageType(OutageType.SCHEDULED)
                .status(OutageStatus.SCHEDULED)
                .reason("Transformer upgrade in sector 4")
                .startTime(now.plusHours(1))
                .endTime(now.plusHours(4))
                .createdAt(now)
                .updatedAt(now)
                .build();

        sampleRequest = OutageRequest.builder()
                .title("Substation Grid Maintenance")
                .area(Area.DHANMONDI)
                .outageType(OutageType.SCHEDULED)
                .status(OutageStatus.SCHEDULED)
                .reason("Transformer upgrade in sector 4")
                .startTime(now.plusHours(1))
                .endTime(now.plusHours(4))
                .build();
    }

    @Test
    @DisplayName("createOutage saves entity, notifies client, and returns DTO")
    void testCreateOutage_Success() {
        when(outageRepository.save(any(Outage.class))).thenReturn(sampleOutage);

        OutageResponse response = outageService.createOutage(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(outageId);
        assertThat(response.getArea()).isEqualTo(Area.DHANMONDI);
        assertThat(response.getStatus()).isEqualTo(OutageStatus.SCHEDULED);

        verify(outageRepository, times(1)).save(any(Outage.class));
        verify(notificationClient, times(1)).sendOutageNotification(eq("DHANMONDI"), anyString(), anyString(), eq(outageId));
    }

    @Test
    @DisplayName("createOutage throws IllegalArgumentException if end time is before start time")
    void testCreateOutage_InvalidTimes() {
        sampleRequest.setEndTime(now.minusHours(1));

        assertThatThrownBy(() -> outageService.createOutage(sampleRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time must be after start time");

        verify(outageRepository, never()).save(any(Outage.class));
    }

    @Test
    @DisplayName("getOutageById returns outage when found")
    void testGetOutageById_Found() {
        when(outageRepository.findById(outageId)).thenReturn(Optional.of(sampleOutage));

        OutageResponse response = outageService.getOutageById(outageId);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Substation Grid Maintenance");
    }

    @Test
    @DisplayName("getOutageById throws ResourceNotFoundException when missing")
    void testGetOutageById_NotFound() {
        when(outageRepository.findById(outageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> outageService.getOutageById(outageId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Outage not found");
    }

    @Test
    @DisplayName("getOutagesByArea filters properly")
    void testGetOutagesByArea() {
        when(outageRepository.findByAreaOrderByStartTimeDesc(Area.DHANMONDI))
                .thenReturn(List.of(sampleOutage));

        List<OutageResponse> list = outageService.getOutagesByArea(Area.DHANMONDI);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getArea()).isEqualTo(Area.DHANMONDI);
    }

    @Test
    @DisplayName("updateOutageStatus changes status and notifies client")
    void testUpdateOutageStatus() {
        when(outageRepository.findById(outageId)).thenReturn(Optional.of(sampleOutage));
        when(outageRepository.save(any(Outage.class))).thenReturn(sampleOutage);

        OutageResponse response = outageService.updateOutageStatus(outageId, OutageStatus.RESOLVED);

        assertThat(response.getStatus()).isEqualTo(OutageStatus.RESOLVED);
        verify(notificationClient, times(1)).sendOutageNotification(eq("DHANMONDI"), contains("Status Update"), anyString(), eq(outageId));
    }

    @Test
    @DisplayName("deleteOutage deletes entity when found")
    void testDeleteOutage() {
        when(outageRepository.findById(outageId)).thenReturn(Optional.of(sampleOutage));

        outageService.deleteOutage(outageId);

        verify(outageRepository, times(1)).delete(sampleOutage);
    }
}
