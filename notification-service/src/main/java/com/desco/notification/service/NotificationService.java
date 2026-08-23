package com.desco.notification.service;

import com.desco.notification.dto.request.NotificationRequest;
import com.desco.notification.dto.response.NotificationResponse;
import com.desco.notification.enums.Area;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse createNotification(NotificationRequest request);

    NotificationResponse getNotificationById(UUID id);

    List<NotificationResponse> getAllNotifications();

    List<NotificationResponse> getUserNotifications(UUID userId, Area area);

    List<NotificationResponse> getAreaNotifications(Area area);

    long getUnreadCount(UUID userId);

    NotificationResponse markAsRead(UUID id);

    void markAllAsReadForUser(UUID userId);

    void deleteNotification(UUID id);
}
