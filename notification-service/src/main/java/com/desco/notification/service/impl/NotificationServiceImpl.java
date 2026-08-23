package com.desco.notification.service.impl;

import com.desco.notification.dto.request.NotificationRequest;
import com.desco.notification.dto.response.NotificationResponse;
import com.desco.notification.entity.Notification;
import com.desco.notification.enums.Area;
import com.desco.notification.exception.ResourceNotFoundException;
import com.desco.notification.repository.NotificationRepository;
import com.desco.notification.service.NotificationService;
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
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .targetUserId(request.getTargetUserId())
                .targetArea(request.getTargetArea())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .isRead(false)
                .referenceId(request.getReferenceId())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created notification {} of type {} (targetUser={}, targetArea={})",
                saved.getId(), saved.getType(), saved.getTargetUserId(), saved.getTargetArea());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return mapToResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(UUID userId, Area area) {
        List<Notification> notifications;
        if (area != null) {
            notifications = notificationRepository.findUserFeed(userId, area);
        } else {
            notifications = notificationRepository.findUserAndBroadcastNotifications(userId);
        }

        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAreaNotifications(Area area) {
        return notificationRepository.findByTargetAreaOrderByCreatedAtDesc(area).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByTargetUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        notification.setIsRead(true);
        Notification updated = notificationRepository.save(notification);
        log.info("Marked notification {} as read", id);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void markAllAsReadForUser(UUID userId) {
        int count = notificationRepository.markAllAsReadForUser(userId);
        log.info("Marked {} notifications as read for user {}", count, userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notificationRepository.delete(notification);
        log.info("Deleted notification {}", id);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .targetUserId(notification.getTargetUserId())
                .targetArea(notification.getTargetArea())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
