package com.tenanthub.billing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenanthub.billing.entity.ActivityLog;
import com.tenanthub.billing.repository.ActivityLogRepository;
import com.tenanthub.events.TaskCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ActivityLogService {

    private static final String TASK_CREATED_EVENT_TYPE = "TASK_CREATED";

    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;

    public void recordTaskCreated(TaskCreatedEvent event) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize TaskCreatedEvent for tenant {}", event.tenantId(), e);
            throw new IllegalStateException("Failed to serialize TaskCreatedEvent", e);
        }

        activityLogRepository.save(ActivityLog.builder()
                .eventType(TASK_CREATED_EVENT_TYPE)
                .tenantId(event.tenantId())
                .payload(payload)
                .build());
    }
}
