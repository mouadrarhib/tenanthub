package com.tenanthub.billing.listener;

import com.tenanthub.billing.service.UsageTrackingService;
import com.tenanthub.events.EventTopics;
import com.tenanthub.events.ProjectCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectEventListener {

    private final UsageTrackingService usageTrackingService;

    @KafkaListener(topics = EventTopics.PROJECT_CREATED)
    public void onProjectCreated(ProjectCreatedEvent event) {
        try {
            usageTrackingService.recordProjectCreated(event);
        } catch (Exception e) {
            log.error("Failed to process project.created event for project {}", event.projectId(), e);
        }
    }
}
