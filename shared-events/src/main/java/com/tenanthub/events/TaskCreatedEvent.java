package com.tenanthub.events;

import java.time.Instant;
import java.util.UUID;

public record TaskCreatedEvent(
        UUID taskId,
        UUID projectId,
        UUID tenantId,
        String title,
        UUID assigneeUserId,
        Instant occurredAt
) {
}
