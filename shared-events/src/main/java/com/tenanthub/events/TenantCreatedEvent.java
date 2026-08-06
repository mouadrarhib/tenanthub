package com.tenanthub.events;

import java.time.Instant;
import java.util.UUID;

public record TenantCreatedEvent(
        UUID tenantId,
        String name,
        UUID planId,
        String planName,
        int maxUsers,
        int maxProjects,
        Instant occurredAt
) {
}
