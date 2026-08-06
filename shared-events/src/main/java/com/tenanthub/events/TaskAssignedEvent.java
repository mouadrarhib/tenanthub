package com.tenanthub.events;

import java.time.Instant;
import java.util.UUID;

public record TaskAssignedEvent(
        UUID taskId,
        UUID projectId,
        UUID tenantId,
        UUID assigneeUserId,
        Instant occurredAt
) {
}
