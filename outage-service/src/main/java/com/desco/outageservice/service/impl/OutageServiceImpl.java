package com.desco.outageservice.service.impl;

import com.desco.outageservice.client.NotificationClient;
import com.desco.outageservice.dto.request.CreateOutageRequest;
import com.desco.outageservice.dto.request.UpdateOutageRequest;
import com.desco.outageservice.dto.request.UpdateOutageStatusRequest;
import com.desco.outageservice.dto.response.OutageResponse;
import com.desco.outageservice.entity.Outage;
import com.desco.outageservice.enums.Area;
import com.desco.outageservice.enums.OutageStatus;
import com.desco.outageservice.exception.BadRequestException;
import com.desco.outageservice.exception.ConflictException;
import com.desco.outageservice.exception.ResourceNotFoundException;
import com.desco.outageservice.repository.OutageRepository;
import com.desco.outageservice.security.UserPrincipal;
import com.desco.outageservice.service.OutageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutageServiceImpl implements OutageService {

    private final OutageRepository outageRepository;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public OutageResponse createOutage(CreateOutageRequest request, UserPrincipal admin) {
        OutageStatus status = request.getStatus() != null ? request.getStatus() : OutageStatus.SCHEDULED;

        Outage outage = Outage.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .area(request.getArea())
                .type(request.getType())
                .status(status)
                .startTime(request.getStartTime())
                .estimatedEndTime(request.getEstimatedEndTime())
                .createdBy(admin.getId())
                .build();

        Outage saved = outageRepository.save(outage);
        log.info("Created outage: id={}, area={}, type={}", saved.getId(), saved.getArea(), saved.getType());

        String message = saved.getDescription() != null && !saved.getDescription().isBlank()
                ? saved.getDescription()
                : "Power outage (" + saved.getType() + ") scheduled for " + saved.getArea() + " starting at " + saved.getStartTime();

        notificationClient.broadcastOutageNotification(
                saved.getArea(),
                "Power Outage Alert: " + saved.getTitle(),
                message,
                saved.getId()
        );

        return OutageResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OutageResponse> getAllOutages(Pageable pageable) {
        return outageRepository.findAll(pageable).map(OutageResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public OutageResponse getOutageById(UUID id) {
        return OutageResponse.fromEntity(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OutageResponse> getMyAreaOutages(UserPrincipal user, Pageable pageable) {
        if (user.getArea() == null) {
            throw new BadRequestException("No area assigned to current user profile.");
        }
        return outageRepository.findByAreaActiveFirstPaged(user.getArea(), pageable)
                .map(OutageResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OutageResponse> getOutagesByArea(Area area, Pageable pageable) {
        return outageRepository.findByArea(area, pageable).map(OutageResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OutageResponse> getOutageHistory(UserPrincipal user, Pageable pageable) {
        return outageRepository.findHistory(user != null ? user.getArea() : null, pageable)
                .map(OutageResponse::fromEntity);
    }

    @Override
    @Transactional
    public OutageResponse updateOutage(UUID id, UpdateOutageRequest request) {
        Outage outage = findOrThrow(id);

        outage.setTitle(request.getTitle());
        outage.setDescription(request.getDescription());
        outage.setArea(request.getArea());
        outage.setType(request.getType());
        outage.setStatus(request.getStatus());
        outage.setStartTime(request.getStartTime());
        outage.setEstimatedEndTime(request.getEstimatedEndTime());
        outage.setActualEndTime(request.getActualEndTime());

        Outage saved = outageRepository.save(outage);
        log.info("Updated outage details for id: {}", id);
        return OutageResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public OutageResponse updateOutageStatus(UUID id, UpdateOutageStatusRequest request) {
        Outage outage = findOrThrow(id);
        OutageStatus newStatus = request.getStatus();

        outage.setStatus(newStatus);
        if (newStatus == OutageStatus.RESOLVED && outage.getActualEndTime() == null) {
            outage.setActualEndTime(Instant.now());
        }

        Outage saved = outageRepository.save(outage);
        log.info("Updated outage status to {} for id: {}", newStatus, id);

        String message = "Outage status for '" + saved.getTitle() + "' in " + saved.getArea() + " has been updated to " + newStatus;
        notificationClient.broadcastOutageNotification(
                saved.getArea(),
                "Outage Status Update: " + saved.getTitle(),
                message,
                saved.getId()
        );

        return OutageResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteOutage(UUID id) {
        Outage outage = findOrThrow(id);

        if (outage.getStatus() != OutageStatus.SCHEDULED || !outage.getStartTime().isAfter(Instant.now())) {
            throw new ConflictException("Can only delete an outage if it is in SCHEDULED status and has not yet started.");
        }

        outageRepository.delete(outage);
        log.info("Deleted outage with id: {}", id);
    }

    private Outage findOrThrow(UUID id) {
        return outageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Outage not found with id: " + id));
    }
}
