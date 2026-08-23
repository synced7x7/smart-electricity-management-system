package com.desco.outage.entity;

import com.desco.outage.enums.Area;
import com.desco.outage.enums.OutageStatus;
import com.desco.outage.enums.OutageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Maps the pre-existing, hand-designed `outages` table. Every column below is matched
 * to the real schema exactly — an earlier version of this entity invented column names
 * (`outage_type`, `reason`) that do not exist and omitted the NOT NULL `created_by`,
 * so every insert failed. `area`/`type`/`status` are native PostgreSQL enum types, so
 * writes need an explicit cast via @ColumnTransformer; a plain @Enumerated binds them
 * as varchar and Postgres rejects it.
 */
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
    @Column(nullable = false, columnDefinition = "area_name")
    @ColumnTransformer(write = "?::area_name")
    private Area area;

    /** Java-side name kept as `outageType` (the DTOs use it); the column is `type`. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "outage_type")
    @ColumnTransformer(write = "?::outage_type")
    private OutageType outageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "outage_status")
    @ColumnTransformer(write = "?::outage_status")
    private OutageStatus status;

    /** Java-side name kept as `reason` (the DTOs use it); the column is `description`. */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /** NOT NULL — the id of the admin who scheduled this outage, taken from the JWT. */
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
