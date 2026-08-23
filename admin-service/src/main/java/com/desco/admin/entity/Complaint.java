package com.desco.admin.entity;

import com.desco.admin.enums.AreaName;
import com.desco.admin.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "complaints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    // Native PostgreSQL enum (area_name).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "area_name")
    @ColumnTransformer(write = "?::area_name")
    private AreaName area;

    // Native PostgreSQL enum (complaint_status) — admins update this field.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "complaint_status")
    @ColumnTransformer(write = "?::complaint_status")
    private ComplaintStatus status;

    @Column(name = "admin_remark", columnDefinition = "text")
    private String adminRemark;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
