package com.desco.complaintservice.service.impl;

import com.desco.complaintservice.client.NotificationClient;
import com.desco.complaintservice.dto.request.CreateComplaintRequest;
import com.desco.complaintservice.dto.request.UpdateComplaintRequest;
import com.desco.complaintservice.dto.request.UpdateComplaintStatusRequest;
import com.desco.complaintservice.dto.response.ComplaintResponse;
import com.desco.complaintservice.entity.Complaint;
import com.desco.complaintservice.enums.Area;
import com.desco.complaintservice.enums.ComplaintStatus;
import com.desco.complaintservice.exception.BadRequestException;
import com.desco.complaintservice.exception.ConflictException;
import com.desco.complaintservice.exception.ResourceNotFoundException;
import com.desco.complaintservice.repository.ComplaintRepository;
import com.desco.complaintservice.security.UserPrincipal;
import com.desco.complaintservice.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public ComplaintResponse createComplaint(CreateComplaintRequest request, UserPrincipal user) {
        if (user.getArea() == null) {
            throw new BadRequestException("No area assigned to current user profile.");
        }

        Complaint complaint = Complaint.builder()
                .userId(user.getId())
                .area(user.getArea())
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(ComplaintStatus.SUBMITTED)
                .build();

        Complaint saved = complaintRepository.save(complaint);
        log.info("Created complaint: id={}, userId={}, area={}", saved.getId(), saved.getUserId(), saved.getArea());
        return ComplaintResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getMyComplaints(UserPrincipal user, Pageable pageable) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(ComplaintResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(UUID id, UserPrincipal user) {
        Complaint complaint = findOrThrow(id);
        verifyAccess(complaint, user);
        return ComplaintResponse.fromEntity(complaint);
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaint(UUID id, UpdateComplaintRequest request, UserPrincipal user) {
        Complaint complaint = findOrThrow(id);

        if (!complaint.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You are not authorized to edit this complaint.");
        }

        if (complaint.getStatus() != ComplaintStatus.SUBMITTED) {
            throw new ConflictException("Complaint can only be edited while it is in SUBMITTED status.");
        }

        complaint.setSubject(request.getSubject());
        complaint.setDescription(request.getDescription());

        Complaint saved = complaintRepository.save(complaint);
        log.info("User {} updated complaint {}", user.getId(), id);
        return ComplaintResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteComplaint(UUID id, UserPrincipal user) {
        Complaint complaint = findOrThrow(id);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        if (!isAdmin) {
            if (!complaint.getUserId().equals(user.getId())) {
                throw new AccessDeniedException("You are not authorized to delete this complaint.");
            }
            if (complaint.getStatus() != ComplaintStatus.SUBMITTED) {
                throw new ConflictException("Complaint can only be deleted by user while it is in SUBMITTED status.");
            }
        }

        complaintRepository.delete(complaint);
        log.info("Deleted complaint {} by user {}", id, user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getAllComplaints(Pageable pageable) {
        return complaintRepository.findAll(pageable).map(ComplaintResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getComplaintsByArea(Area area, Pageable pageable) {
        return complaintRepository.findByAreaOrderByCreatedAtDesc(area, pageable)
                .map(ComplaintResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getComplaintsByStatus(ComplaintStatus status, Pageable pageable) {
        return complaintRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(ComplaintResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> searchComplaints(String query, Pageable pageable) {
        return complaintRepository.searchComplaints(query != null ? query.trim() : "", pageable)
                .map(ComplaintResponse::fromEntity);
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaintStatus(UUID id, UpdateComplaintStatusRequest request) {
        Complaint complaint = findOrThrow(id);
        ComplaintStatus newStatus = request.getStatus();

        complaint.setStatus(newStatus);
        if (request.getAdminRemarks() != null) {
            complaint.setAdminRemarks(request.getAdminRemarks());
        }

        if (newStatus == ComplaintStatus.RESOLVED || newStatus == ComplaintStatus.REJECTED) {
            complaint.setResolvedAt(Instant.now());
        }

        Complaint saved = complaintRepository.save(complaint);
        log.info("Admin updated complaint {} status to {}", id, newStatus);

        String remarksPart = saved.getAdminRemarks() != null && !saved.getAdminRemarks().isBlank()
                ? " (Remarks: " + saved.getAdminRemarks() + ")"
                : "";
        String message = "Your complaint '" + saved.getSubject() + "' status has been updated to: " + newStatus + remarksPart;

        notificationClient.notifyComplaintStatusUpdate(
                saved.getUserId(),
                saved.getArea(),
                "Complaint Status Update: " + saved.getSubject(),
                message,
                saved.getId()
        );

        return ComplaintResponse.fromEntity(saved);
    }

    private Complaint findOrThrow(UUID id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
    }

    private void verifyAccess(Complaint complaint, UserPrincipal user) {
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return;
        }
        if (!complaint.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to this complaint.");
        }
    }
}
