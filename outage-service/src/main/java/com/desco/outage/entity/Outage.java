package com.desco.outage.entity;

import com.desco.outage.enums.Area;
import com.desco.outage.enums.OutageStatus;
import com.desco.outage.enums.OutageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Outage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Area area;

    @Enumerated(EnumType.STRING)
    @Column(name = "outage_type", nullable = false, length = 50)
    private OutageType outageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OutageStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
