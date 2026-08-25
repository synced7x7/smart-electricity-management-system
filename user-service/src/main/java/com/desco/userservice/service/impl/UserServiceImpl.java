package com.desco.userservice.service.impl;

import com.desco.userservice.dto.request.UpdateProfileRequest;
import com.desco.userservice.dto.response.UserProfileResponse;
import com.desco.userservice.entity.User;
import com.desco.userservice.entity.UserProfile;
import com.desco.userservice.enums.AreaName;
import com.desco.userservice.exception.DuplicateMeterNumberException;
import com.desco.userservice.exception.ResourceNotFoundException;
import com.desco.userservice.repository.UserProfileRepository;
import com.desco.userservice.repository.UserRepository;
import com.desco.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public UserProfileResponse getOwnProfile(User caller) {
        UserProfile profile = userProfileRepository.findByUserId(caller.getId()).orElse(null);
        return toResponse(caller, profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateOwnProfile(User caller, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(caller.getId()).orElse(null);

        if (profile == null) {
            // full_name and area are NOT NULL at the DB level — required on first
            // creation only; a fresh profile falls back to the account's own area if
            // the request doesn't specify one.
            if (request.getFullName() == null || request.getFullName().isBlank()) {
                throw new IllegalArgumentException("fullName is required to create a profile");
            }
            AreaName initialArea = request.getArea() != null ? request.getArea() : caller.getArea();
            if (initialArea == null) {
                throw new IllegalArgumentException(
                        "area is required to create a profile (your account has no area on file either)");
            }
            profile = UserProfile.builder()
                    .userId(caller.getId())
                    .fullName(request.getFullName())
                    .area(initialArea)
                    .build();
        }

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhone(request.getPhoneNumber());
        }
        if (request.getArea() != null) {
            profile.setArea(request.getArea());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getMeterNumber() != null) {
            profile.setMeterNumber(request.getMeterNumber());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        try {
            UserProfile saved = userProfileRepository.saveAndFlush(profile);
            return toResponse(caller, saved);
        } catch (DataIntegrityViolationException ex) {
            // Only claim "duplicate meter number" when that is actually the
            // constraint that fired. This used to translate EVERY integrity
            // violation, which produced genuinely misleading errors once other
            // constraints existed — an unknown enum label reported itself as
            // "Meter number 'null' is already registered to another account".
            // (Documented as backend issue #22.)
            if (isMeterNumberConflict(ex)) {
                throw new DuplicateMeterNumberException(
                        "Meter number '" + request.getMeterNumber() + "' is already registered to another account");
            }
            throw ex;
        }
    }

    @Override
    public UserProfileResponse getProfileByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        return toResponse(user, profile);
    }

    /**
     * True only for the user_profiles.meter_number UNIQUE constraint.
     *
     * Matching on the bare column name is NOT enough: Hibernate puts the whole
     * generated INSERT in the exception message, and that statement lists every
     * column — so any failure on this table (an unknown enum label, a foreign
     * key) contains the text "meter_number" and would be misreported as a
     * duplicate meter. Match the constraint name, or the unique-violation
     * wording together with the column.
     */
    private static boolean isMeterNumberConflict(DataIntegrityViolationException ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            String message = t.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("user_profiles_meter_number_key")) {
                    return true;
                }
                boolean uniqueViolation = lower.contains("duplicate key value")
                        || lower.contains("violates unique constraint");
                if (uniqueViolation && lower.contains("meter_number")) {
                    return true;
                }
            }
            if (t.getCause() == t) break;
        }
        return false;
    }

    private UserProfileResponse toResponse(User user, UserProfile profile) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .fullName(profile != null ? profile.getFullName() : null)
                .phoneNumber(profile != null ? profile.getPhone() : null)
                .area(profile != null && profile.getArea() != null ? profile.getArea().name() : null)
                .address(profile != null ? profile.getAddress() : null)
                .meterNumber(profile != null ? profile.getMeterNumber() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .accountCreatedAt(user.getCreatedAt())
                .profileUpdatedAt(profile != null ? profile.getUpdatedAt() : null)
                .build();
    }
}
