package com.tenanthub.project.event;

import com.tenanthub.events.EventTopics;
import com.tenanthub.events.ProjectCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishProjectCreated(ProjectCreatedEvent event) {
        kafkaTemplate.send(EventTopics.PROJECT_CREATED, event.projectId().toString(), event);
    }
}
