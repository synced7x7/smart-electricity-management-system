package com.desco.complaint.entity;

import com.desco.complaint.enums.Area;
import com.desco.complaint.enums.ComplaintCategory;
import com.desco.complaint.enums.ComplaintStatus;
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
 * Maps the pre-existing, hand-designed `complaints` table.
 *
 * <p>The Java field names {@code title} and {@code resolutionNotes} are kept (the DTOs
 * and API contract use them) but they map to the ORIGINAL columns {@code subject} and
 * {@code admin_remark}. An earlier run with {@code ddl-auto: update} had added duplicate
 * `title`/`resolution_notes` columns alongside them, which split the data in two:
 * admin-service writes an admin's response to `admin_remark` while this service read
 * `resolution_notes`, so neither ever saw the other's data. Both services now share
 * one column.
 *
 * <p>{@code area} and {@code status} are native PostgreSQL enum types — writes need an
 * explicit cast, otherwise Postgres rejects the varchar bind parameter.
 */
@Entity
@Table(name = "complaints")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "area_name")
    @ColumnTransformer(write = "?::area_name")
    private Area area;

    /** Plain varchar in the schema, not a native enum — no cast needed. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ComplaintCategory category;

    /** Java-side name kept as `title`; the column is `subject`. */
    @Column(name = "subject", nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "complaint_status")
    @ColumnTransformer(write = "?::complaint_status")
    private ComplaintStatus status;

    /** Java-side name kept as `resolutionNotes`; the column is `admin_remark`. */
    @Column(name = "admin_remark", columnDefinition = "TEXT")
    private String resolutionNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
