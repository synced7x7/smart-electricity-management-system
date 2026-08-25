package com.desco.outage.security;

import java.util.UUID;

/**
 * Security principal for an authenticated caller.
 *
 * <p>Carries the {@code userId} claim alongside the email because {@code outages.created_by}
 * is NOT NULL and must record which admin scheduled the outage. Previously the principal
 * was just the email string, so there was no way to populate that column — which is part
 * of why every outage insert failed (the entity omitted the field entirely).
 */
public record AuthenticatedUser(UUID userId, String email) {
}
