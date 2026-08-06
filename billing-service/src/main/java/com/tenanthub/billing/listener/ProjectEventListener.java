package com.tenanthub.billing.listener;

import com.tenanthub.billing.service.UsageTrackingService;
import com.tenanthub.events.EventTopics;
import com.tenanthub.events.ProjectCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectEventListener {

    private final UsageTrackingService usageTrackingService;

    // Exceptions propagate on purpose - KafkaErrorHandlingConfig's DefaultErrorHandler
    // retries a couple of times, then routes the record to project.created.DLT instead
    // of retrying forever or silently dropping it.
    @KafkaListener(topics = EventTopics.PROJECT_CREATED)
    public void onProjectCreated(ProjectCreatedEvent event) {
        usageTrackingService.recordProjectCreated(event);
    }
}
