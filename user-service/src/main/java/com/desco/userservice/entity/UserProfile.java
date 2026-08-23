package com.desco.userservice.entity;

import com.desco.userservice.enums.AreaName;
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
 * Profile fields beyond what auth-service's `users` table carries. This table
 * already existed with a hand-designed schema before this service was implemented —
 * FK to users(id) ON DELETE CASCADE, UNIQUE on meter_number, a DB trigger maintaining
 * updated_at — so ddl-auto is `none` and every column here must match exactly.
 * Not every user has a row yet; one is created lazily on first PUT (see
 * UserServiceImpl.updateOwnProfile).
 */
@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 20)
    private String phone;

    // Native PostgreSQL enum (area_name) — required by the schema (NOT NULL).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "area_name")
    @ColumnTransformer(write = "?::area_name")
    private AreaName area;

    /** DESCO electricity meter number for this account. UNIQUE at the DB level. */
    @Column(name = "meter_number", length = 50)
    private String meterNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Also maintained by a DB trigger (trg_user_profiles_updated_at) — harmless overlap,
    // Hibernate's value is what UPDATE ... RETURNING would show before the trigger fires.
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
