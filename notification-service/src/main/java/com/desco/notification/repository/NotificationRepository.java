package com.desco.notification.repository;

import com.desco.notification.entity.Notification;
import com.desco.notification.enums.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByTargetUserIdOrderByCreatedAtDesc(UUID targetUserId);

    List<Notification> findByTargetAreaOrderByCreatedAtDesc(Area targetArea);

    @Query("SELECT n FROM Notification n WHERE n.targetUserId = :userId OR (n.targetUserId IS NULL AND (n.targetArea = :area OR n.targetArea IS NULL)) ORDER BY n.createdAt DESC")
    List<Notification> findUserFeed(@Param("userId") UUID userId, @Param("area") Area area);

    @Query("SELECT n FROM Notification n WHERE n.targetUserId = :userId OR n.targetUserId IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findUserAndBroadcastNotifications(@Param("userId") UUID userId);

    long countByTargetUserIdAndIsReadFalse(UUID targetUserId);

    List<Notification> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.targetUserId = :userId AND n.isRead = false")
    int markAllAsReadForUser(@Param("userId") UUID userId);
}
