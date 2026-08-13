package com.tenanthub.billing.dto;

import com.tenanthub.billing.entity.ActivityLog;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityLogResponse(
        UUID id,
        String eventType,
        String payload,
        LocalDateTime receivedAt
) {

    public static ActivityLogResponse of(ActivityLog activityLog) {
        return new ActivityLogResponse(
                activityLog.getId(),
                activityLog.getEventType(),
                activityLog.getPayload(),
                activityLog.getReceivedAt()
        );
    }
}
