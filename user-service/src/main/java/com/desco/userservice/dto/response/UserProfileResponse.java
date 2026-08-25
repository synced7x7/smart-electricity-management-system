package com.desco.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** Merges account fields (owned by auth-service) with profile fields (owned by this service). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private UUID userId;
    private String email;
    private String role;
    private Boolean isActive;

    private String fullName;
    private String phoneNumber;
    private String area;
    private String address;
    private String meterNumber;
    private String avatarUrl;

    private LocalDateTime accountCreatedAt;
    private LocalDateTime profileUpdatedAt;
}
