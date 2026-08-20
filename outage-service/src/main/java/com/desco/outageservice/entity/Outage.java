package com.desco.outageservice.entity;

import com.desco.outageservice.enums.Area;
import com.desco.outageservice.enums.OutageStatus;
import com.desco.outageservice.enums.OutageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Outage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "area", columnDefinition = "area_name", nullable = false)
    private Area area;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", columnDefinition = "outage_type", nullable = false)
    private OutageType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "outage_status", nullable = false)
    private OutageStatus status;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "estimated_end_time")
    private Instant estimatedEndTime;

    @Column(name = "actual_end_time")
    private Instant actualEndTime;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
