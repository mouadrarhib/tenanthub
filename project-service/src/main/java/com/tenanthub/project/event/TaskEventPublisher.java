package com.tenanthub.project.event;

import com.tenanthub.events.EventTopics;
import com.tenanthub.events.TaskAssignedEvent;
import com.tenanthub.events.TaskCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTaskCreated(TaskCreatedEvent event) {
        kafkaTemplate.send(EventTopics.TASK_CREATED, event.taskId().toString(), event);
    }

    public void publishTaskAssigned(TaskAssignedEvent event) {
        kafkaTemplate.send(EventTopics.TASK_ASSIGNED, event.taskId().toString(), event);
    }
}
