package com.desco.userservice.controller;

import com.desco.userservice.dto.request.UpdateProfileRequest;
import com.desco.userservice.dto.response.UserProfileResponse;
import com.desco.userservice.entity.User;
import com.desco.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile management, separate from auth-service's authentication concern")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated caller's own profile")
    public ResponseEntity<UserProfileResponse> getOwnProfile(@AuthenticationPrincipal User caller) {
        return ResponseEntity.ok(userService.getOwnProfile(caller));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the authenticated caller's own profile (creates it on first use)")
    public ResponseEntity<UserProfileResponse> updateOwnProfile(
            @AuthenticationPrincipal User caller,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateOwnProfile(caller, request));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get any user's profile by id (for cross-service lookups, e.g. by area or name)")
    public ResponseEntity<UserProfileResponse> getProfileByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getProfileByUserId(userId));
    }
}
