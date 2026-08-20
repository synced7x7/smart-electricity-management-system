package com.desco.notificationservice.repository;

import com.desco.notificationservice.entity.Notification;
import com.desco.notificationservice.enums.Area;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId OR (:area IS NOT NULL AND n.area = :area) ORDER BY n.createdAt DESC")
    Page<Notification> findMyNotifications(@Param("userId") UUID userId, @Param("area") Area area, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.userId = :userId OR (:area IS NOT NULL AND n.area = :area)) AND n.isRead = false")
    long countUnreadMyNotifications(@Param("userId") UUID userId, @Param("area") Area area);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE (n.userId = :userId OR (:area IS NOT NULL AND n.area = :area)) AND n.isRead = false")
    int markAllAsRead(@Param("userId") UUID userId, @Param("area") Area area);
}
