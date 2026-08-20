package com.desco.notificationservice.service.impl;

import com.desco.notificationservice.dto.request.BroadcastNotificationRequest;
import com.desco.notificationservice.dto.response.NotificationResponse;
import com.desco.notificationservice.entity.Notification;
import com.desco.notificationservice.exception.BadRequestException;
import com.desco.notificationservice.exception.ResourceNotFoundException;
import com.desco.notificationservice.repository.NotificationRepository;
import com.desco.notificationservice.security.UserPrincipal;
import com.desco.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(UserPrincipal user, Pageable pageable) {
        log.debug("Fetching notifications for user={}, area={}", user.getId(), user.getArea());
        return notificationRepository
                .findMyNotifications(user.getId(), user.getArea(), pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UserPrincipal user) {
        return notificationRepository.countUnreadMyNotifications(user.getId(), user.getArea());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID id, UserPrincipal user) {
        Notification notification = findOrThrow(id);
        verifyAccess(notification, user);
        return NotificationResponse.fromEntity(notification);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID id, UserPrincipal user) {
        Notification notification = findOrThrow(id);
        verifyAccess(notification, user);
        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        log.info("Marked notification {} as read for user {}", id, user.getId());
        return NotificationResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void markAllAsRead(UserPrincipal user) {
        int updated = notificationRepository.markAllAsRead(user.getId(), user.getArea());
        log.info("Marked {} notifications as read for user {}", updated, user.getId());
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id, UserPrincipal user) {
        Notification notification = findOrThrow(id);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        boolean isOwner = notification.getUserId() != null && notification.getUserId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Only the owner or an admin can delete this notification.");
        }

        notificationRepository.delete(notification);
        log.info("Deleted notification {} by user {}", id, user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional
    public NotificationResponse broadcastNotification(BroadcastNotificationRequest request) {
        if (request.getUserId() == null && request.getArea() == null) {
            throw new BadRequestException("At least one of userId or area must be specified for notification broadcast.");
        }

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .area(request.getArea())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .isRead(false)
                .relatedEntityId(request.getRelatedEntityId())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Broadcast notification created: id={}, type={}, userId={}, area={}",
                saved.getId(), saved.getType(), saved.getUserId(), saved.getArea());
        return NotificationResponse.fromEntity(saved);
    }

    private Notification findOrThrow(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
    }

    private void verifyAccess(Notification notification, UserPrincipal user) {
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return;
        }
        boolean isOwner = notification.getUserId() != null && notification.getUserId().equals(user.getId());
        boolean isSameArea = notification.getArea() != null && notification.getArea().equals(user.getArea());

        if (!isOwner && !isSameArea) {
            throw new AccessDeniedException("You do not have access to this notification.");
        }
    }
}
