package com.tenanthub.billing.listener;

import com.tenanthub.billing.service.ActivityLogService;
import com.tenanthub.events.EventTopics;
import com.tenanthub.events.TaskCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final ActivityLogService activityLogService;

    // Exceptions propagate on purpose - KafkaErrorHandlingConfig's DefaultErrorHandler
    // retries a couple of times, then routes the record to task.created-dlt instead
    // of retrying forever or silently dropping it.
    @KafkaListener(topics = EventTopics.TASK_CREATED, groupId = "billing-service")
    public void onTaskCreated(TaskCreatedEvent event) {
        activityLogService.recordTaskCreated(event);
    }
}
