package com.tenanthub.events;

import java.time.Instant;
import java.util.UUID;

public record ProjectCreatedEvent(
        UUID projectId,
        UUID tenantId,
        String name,
        Instant occurredAt
) {
}
