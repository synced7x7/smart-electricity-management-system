package com.desco.complaint.service;

import com.desco.complaint.client.NotificationClient;
import com.desco.complaint.dto.request.ComplaintRequest;
import com.desco.complaint.dto.request.ComplaintUpdateRequest;
import com.desco.complaint.dto.response.ComplaintResponse;
import com.desco.complaint.entity.Complaint;
import com.desco.complaint.enums.Area;
import com.desco.complaint.enums.ComplaintCategory;
import com.desco.complaint.enums.ComplaintStatus;
import com.desco.complaint.exception.ResourceNotFoundException;
import com.desco.complaint.repository.ComplaintRepository;
import com.desco.complaint.service.impl.ComplaintServiceImpl;
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
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private ComplaintServiceImpl complaintService;

    private Complaint sampleComplaint;
    private ComplaintRequest sampleRequest;
    private UUID complaintId;
    private UUID userId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        complaintId = UUID.randomUUID();
        userId = UUID.randomUUID();
        now = LocalDateTime.now();

        sampleComplaint = Complaint.builder()
                .id(complaintId)
                .userId(userId)
                .area(Area.BANANI)
                .category(ComplaintCategory.VOLTAGE_FLUCTUATION)
                .title("Frequent voltage spikes")
                .description("Voltage fluctuations damaging appliances in road 11")
                .status(ComplaintStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        sampleRequest = ComplaintRequest.builder()
                .userId(userId)
                .area(Area.BANANI)
                .category(ComplaintCategory.VOLTAGE_FLUCTUATION)
                .title("Frequent voltage spikes")
                .description("Voltage fluctuations damaging appliances in road 11")
                .build();
    }

    @Test
    @DisplayName("submitComplaint creates complaint, sends alert, returns DTO")
    void testSubmitComplaint_Success() {
        when(complaintRepository.saveAndFlush(any(Complaint.class))).thenReturn(sampleComplaint);

        ComplaintResponse response = complaintService.submitComplaint(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(complaintId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getStatus()).isEqualTo(ComplaintStatus.PENDING);

        verify(complaintRepository, times(1)).saveAndFlush(any(Complaint.class));
        verify(notificationClient, times(1)).sendComplaintNotification(eq(userId), eq("BANANI"), anyString(), anyString(), eq(complaintId));
    }

    @Test
    @DisplayName("getComplaintById returns response when found")
    void testGetComplaintById_Found() {
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.of(sampleComplaint));

        ComplaintResponse response = complaintService.getComplaintById(complaintId);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Frequent voltage spikes");
    }

    @Test
    @DisplayName("getComplaintById throws ResourceNotFoundException when missing")
    void testGetComplaintById_NotFound() {
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> complaintService.getComplaintById(complaintId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Complaint not found");
    }

    @Test
    @DisplayName("getComplaintsByUser returns user's complaints")
    void testGetComplaintsByUser() {
        when(complaintRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(sampleComplaint));

        List<ComplaintResponse> list = complaintService.getComplaintsByUser(userId);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("updateComplaintStatus updates status and sends notification")
    void testUpdateComplaintStatus() {
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.of(sampleComplaint));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(sampleComplaint);

        ComplaintResponse response = complaintService.updateComplaintStatus(complaintId, ComplaintStatus.RESOLVED, "Capacitor bank replaced");

        assertThat(response.getStatus()).isEqualTo(ComplaintStatus.RESOLVED);
        verify(notificationClient, times(1)).sendComplaintNotification(eq(userId), eq("BANANI"), contains("Status Updated"), anyString(), eq(complaintId));
    }

    @Test
    @DisplayName("deleteComplaint deletes complaint when exists")
    void testDeleteComplaint() {
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.of(sampleComplaint));

        complaintService.deleteComplaint(complaintId);

        verify(complaintRepository, times(1)).delete(sampleComplaint);
    }
}
