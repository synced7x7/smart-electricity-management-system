package com.desco.outage.security;

import java.util.UUID;


public record AuthenticatedUser(UUID userId, String email) {
}
