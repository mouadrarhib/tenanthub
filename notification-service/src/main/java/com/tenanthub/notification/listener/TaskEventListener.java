package com.tenanthub.notification.listener;

import com.tenanthub.events.EventTopics;
import com.tenanthub.events.TaskAssignedEvent;
import com.tenanthub.notification.client.AuthUserClient;
import com.tenanthub.notification.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskEventListener {

    private final AuthUserClient authUserClient;
    private final EmailService emailService;

    // Broad catch is deliberate here, unlike the REST GlobalExceptionHandlers elsewhere
    // in this codebase: a bad/unreachable-dependency event should be logged and skipped,
    // not left to Spring Kafka's default retry behavior, until the dead-letter topic
    // step formalizes that.
    @KafkaListener(topics = EventTopics.TASK_ASSIGNED)
    public void onTaskAssigned(TaskAssignedEvent event) {
        try {
            authUserClient.findEmail(event.assigneeUserId()).ifPresentOrElse(
                    email -> emailService.send(email, "You've been assigned a task",
                            "You've been assigned a new task (id: " + event.taskId() + ")."),
                    () -> log.warn("No user found for assigneeUserId {} on task {} - skipping notification",
                            event.assigneeUserId(), event.taskId()));
        } catch (Exception e) {
            log.error("Failed to process task.assigned event for task {}", event.taskId(), e);
        }
    }
}
