package com.desco.complaint.service.impl;

import com.desco.complaint.client.NotificationClient;
import com.desco.complaint.dto.request.ComplaintRequest;
import com.desco.complaint.dto.request.ComplaintUpdateRequest;
import com.desco.complaint.dto.response.ComplaintResponse;
import com.desco.complaint.entity.Complaint;
import com.desco.complaint.enums.Area;
import com.desco.complaint.enums.ComplaintStatus;
import com.desco.complaint.exception.ResourceNotFoundException;
import com.desco.complaint.repository.ComplaintRepository;
import com.desco.complaint.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public ComplaintResponse submitComplaint(ComplaintRequest request) {
        Complaint complaint = Complaint.builder()
                .userId(request.getUserId())
                .area(request.getArea())
                .category(request.getCategory())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(ComplaintStatus.PENDING)
                .build();

        Complaint saved = complaintRepository.save(complaint);
        log.info("Saved complaint {} for user {} in area {}", saved.getId(), saved.getUserId(), saved.getArea());

        // Notify user of complaint submission
        String userMsg = String.format("Your complaint [%s] has been registered under ticket #%s. Status: PENDING.",
                saved.getTitle(), saved.getId().toString().substring(0, 8));
        notificationClient.sendComplaintNotification(saved.getUserId(), saved.getArea().name(), "Complaint Registered", userMsg, saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(UUID id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        return mapToResponse(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsByUser(UUID userId) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsByArea(Area area) {
        return complaintRepository.findByAreaOrderByCreatedAtDesc(area).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsByStatus(ComplaintStatus status) {
        return complaintRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaint(UUID id, ComplaintUpdateRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        complaint.setArea(request.getArea());
        complaint.setCategory(request.getCategory());
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());

        Complaint updated = complaintRepository.save(complaint);
        log.info("Updated complaint {}", updated.getId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaintStatus(UUID id, ComplaintStatus status, String resolutionNotes) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        complaint.setStatus(status);
        if (resolutionNotes != null && !resolutionNotes.isBlank()) {
            complaint.setResolutionNotes(resolutionNotes);
        }

        Complaint updated = complaintRepository.save(complaint);
        log.info("Updated complaint {} status to {}", updated.getId(), status);

        // Notify user about status change
        String userMsg = String.format("Your complaint ticket #%s status has been updated to: %s. %s",
                updated.getId().toString().substring(0, 8),
                status,
                updated.getResolutionNotes() != null ? "Notes: " + updated.getResolutionNotes() : "");
        notificationClient.sendComplaintNotification(updated.getUserId(), updated.getArea().name(), "Complaint Status Updated", userMsg, updated.getId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteComplaint(UUID id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        complaintRepository.delete(complaint);
        log.info("Deleted complaint {}", id);
    }

    private ComplaintResponse mapToResponse(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .userId(complaint.getUserId())
                .area(complaint.getArea())
                .category(complaint.getCategory())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .status(complaint.getStatus())
                .resolutionNotes(complaint.getResolutionNotes())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .build();
    }
}
