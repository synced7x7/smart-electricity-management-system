package com.desco.admin.entity;

import com.desco.admin.enums.AreaName;
import com.desco.admin.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    /** Never exposed through the admin API — present only because the column is NOT NULL. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Native PostgreSQL enum (user_role): the bind parameter must be cast explicitly.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "user_role")
    @ColumnTransformer(write = "?::user_role")
    private UserRole role;

    // Native PostgreSQL enum (area_name).
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "area_name")
    @ColumnTransformer(write = "?::area_name")
    private AreaName area;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
