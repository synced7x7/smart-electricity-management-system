package com.desco.admin.entity;

import com.desco.admin.enums.AreaName;
import com.desco.admin.enums.OutageStatus;
import com.desco.admin.enums.OutageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Outage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    // Native PostgreSQL enum (area_name).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "area_name")
    @ColumnTransformer(write = "?::area_name")
    private AreaName area;

    // Native PostgreSQL enum (outage_type).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "outage_type")
    @ColumnTransformer(write = "?::outage_type")
    private OutageType type;

    // Native PostgreSQL enum (outage_status).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "outage_status")
    @ColumnTransformer(write = "?::outage_status")
    private OutageStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /** UUID of the admin who created the outage (taken from the JWT). */
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
