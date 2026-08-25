package com.desco.userservice.service;

import com.desco.userservice.dto.request.UpdateProfileRequest;
import com.desco.userservice.dto.response.UserProfileResponse;
import com.desco.userservice.entity.User;

import java.util.UUID;

public interface UserService {

    /** Merged account + profile view of the authenticated caller. */
    UserProfileResponse getOwnProfile(User caller);

    /** Creates the profile row on first update; updates only the fields provided. */
    UserProfileResponse updateOwnProfile(User caller, UpdateProfileRequest request);

    /** Merged account + profile view of any user — used by other services for lookups. */
    UserProfileResponse getProfileByUserId(UUID userId);
}
