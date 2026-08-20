package com.desco.notificationservice.service;

import com.desco.notificationservice.dto.request.BroadcastNotificationRequest;
import com.desco.notificationservice.dto.response.NotificationResponse;
import com.desco.notificationservice.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    Page<NotificationResponse> getMyNotifications(UserPrincipal user, Pageable pageable);

    long getUnreadCount(UserPrincipal user);

    NotificationResponse getNotificationById(UUID id, UserPrincipal user);

    NotificationResponse markAsRead(UUID id, UserPrincipal user);

    void markAllAsRead(UserPrincipal user);

    void deleteNotification(UUID id, UserPrincipal user);

    Page<NotificationResponse> getAllNotifications(Pageable pageable);

    NotificationResponse broadcastNotification(BroadcastNotificationRequest request);
}
