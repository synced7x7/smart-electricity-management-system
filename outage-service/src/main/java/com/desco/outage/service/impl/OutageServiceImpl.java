package com.desco.outage.service.impl;

import com.desco.outage.client.NotificationClient;
import com.desco.outage.dto.request.OutageRequest;
import com.desco.outage.dto.response.OutageResponse;
import com.desco.outage.entity.Outage;
import com.desco.outage.enums.Area;
import com.desco.outage.enums.OutageStatus;
import com.desco.outage.exception.ResourceNotFoundException;
import com.desco.outage.repository.OutageRepository;
import com.desco.outage.service.OutageService;
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
public class OutageServiceImpl implements OutageService {

    private final OutageRepository outageRepository;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public OutageResponse createOutage(OutageRequest request, UUID createdBy) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        OutageStatus status = request.getStatus() != null ? request.getStatus() : OutageStatus.SCHEDULED;

        Outage outage = Outage.builder()
                .title(request.getTitle())
                .area(request.getArea())
                .outageType(request.getOutageType())
                .status(status)
                .reason(request.getReason())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .createdBy(createdBy)
                .build();

        Outage saved = outageRepository.saveAndFlush(outage);
        log.info("Created outage {} for area {}", saved.getId(), saved.getArea());

        // Notify area
        String alertMessage = String.format("Power outage [%s] scheduled in %s from %s to %s. Reason: %s",
                saved.getOutageType(), saved.getArea(), saved.getStartTime(), saved.getEndTime(), saved.getReason());
        notificationClient.sendOutageNotification(saved.getArea().name(), saved.getTitle(), alertMessage, saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OutageResponse getOutageById(UUID id) {
        Outage outage = outageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Outage not found with id: " + id));
        return mapToResponse(outage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutageResponse> getAllOutages() {
        return outageRepository.findAllByOrderByStartTimeDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutageResponse> getOutagesByArea(Area area) {
        return outageRepository.findByAreaOrderByStartTimeDesc(area.name()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutageResponse> getActiveOutages() {
        return outageRepository.findByStatusInOrderByStartTimeDesc(
                List.of(OutageStatus.SCHEDULED.name(), OutageStatus.ONGOING.name())
        ).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutageResponse> getOutagesByStatus(OutageStatus status) {
        return outageRepository.findByStatusOrderByStartTimeDesc(status.name()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OutageResponse updateOutage(UUID id, OutageRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Outage outage = outageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Outage not found with id: " + id));

        outage.setTitle(request.getTitle());
        outage.setArea(request.getArea());
        outage.setOutageType(request.getOutageType());
        if (request.getStatus() != null) {
            outage.setStatus(request.getStatus());
        }
        outage.setReason(request.getReason());
        outage.setStartTime(request.getStartTime());
        outage.setEndTime(request.getEndTime());

        Outage updated = outageRepository.save(outage);
        log.info("Updated outage {}", updated.getId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public OutageResponse updateOutageStatus(UUID id, OutageStatus status) {
        Outage outage = outageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Outage not found with id: " + id));

        outage.setStatus(status);
        Outage updated = outageRepository.save(outage);
        log.info("Updated outage {} status to {}", updated.getId(), status);

        String alertMessage = String.format("Outage in %s is now %s. Expected/Restored time: %s",
                updated.getArea(), status, updated.getEndTime());
        notificationClient.sendOutageNotification(updated.getArea().name(), "Outage Status Update: " + updated.getTitle(), alertMessage, updated.getId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteOutage(UUID id) {
        Outage outage = outageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Outage not found with id: " + id));
        outageRepository.delete(outage);
        log.info("Deleted outage {}", id);
    }

    private OutageResponse mapToResponse(Outage outage) {
        return OutageResponse.builder()
                .id(outage.getId())
                .title(outage.getTitle())
                .area(outage.getArea())
                .outageType(outage.getOutageType())
                .status(outage.getStatus())
                .reason(outage.getReason())
                .startTime(outage.getStartTime())
                .endTime(outage.getEndTime())
                .createdAt(outage.getCreatedAt())
                .updatedAt(outage.getUpdatedAt())
                .build();
    }
}
