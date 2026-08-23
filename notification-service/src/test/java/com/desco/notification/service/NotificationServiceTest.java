package com.desco.notification.service;

import com.desco.notification.dto.request.NotificationRequest;
import com.desco.notification.dto.response.NotificationResponse;
import com.desco.notification.entity.Notification;
import com.desco.notification.enums.Area;
import com.desco.notification.enums.NotificationType;
import com.desco.notification.exception.ResourceNotFoundException;
import com.desco.notification.repository.NotificationRepository;
import com.desco.notification.service.impl.NotificationServiceImpl;
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
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification sampleNotification;
    private NotificationRequest sampleRequest;
    private UUID notificationId;
    private UUID userId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        notificationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        now = LocalDateTime.now();

        sampleNotification = Notification.builder()
                .id(notificationId)
                .targetUserId(userId)
                .targetArea(Area.GULSHAN)
                .title("Power Outage Alert")
                .message("Scheduled power cut from 2 PM to 5 PM")
                .type(NotificationType.OUTAGE_ALERT)
                .isRead(false)
                .createdAt(now)
                .build();

        sampleRequest = NotificationRequest.builder()
                .targetUserId(userId)
                .targetArea(Area.GULSHAN)
                .title("Power Outage Alert")
                .message("Scheduled power cut from 2 PM to 5 PM")
                .type(NotificationType.OUTAGE_ALERT)
                .build();
    }

    @Test
    @DisplayName("createNotification saves and returns notification DTO")
    void testCreateNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        NotificationResponse response = notificationService.createNotification(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(notificationId);
        assertThat(response.getTitle()).isEqualTo("Power Outage Alert");
        assertThat(response.getIsRead()).isFalse();

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("getNotificationById returns notification when found")
    void testGetNotificationById_Found() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(sampleNotification));

        NotificationResponse response = notificationService.getNotificationById(notificationId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(notificationId);
    }

    @Test
    @DisplayName("getNotificationById throws ResourceNotFoundException when missing")
    void testGetNotificationById_NotFound() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationById(notificationId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Notification not found");
    }

    @Test
    @DisplayName("getUserNotifications returns feed for user and area")
    void testGetUserNotifications() {
        when(notificationRepository.findUserFeed(userId, Area.GULSHAN))
                .thenReturn(List.of(sampleNotification));

        List<NotificationResponse> list = notificationService.getUserNotifications(userId, Area.GULSHAN);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getTargetUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("getUnreadCount returns unread count for user")
    void testGetUnreadCount() {
        when(notificationRepository.countByTargetUserIdAndIsReadFalse(userId)).thenReturn(5L);

        long count = notificationService.getUnreadCount(userId);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("markAsRead marks notification isRead=true")
    void testMarkAsRead() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        NotificationResponse response = notificationService.markAsRead(notificationId);

        assertThat(response.getIsRead()).isTrue();
    }

    @Test
    @DisplayName("deleteNotification deletes entity when found")
    void testDeleteNotification() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(sampleNotification));

        notificationService.deleteNotification(notificationId);

        verify(notificationRepository, times(1)).delete(sampleNotification);
    }
}
