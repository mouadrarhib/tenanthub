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

    // A missing user is a real business outcome (bad/stale id), not a failure - logged
    // and skipped, no retry. Anything else (auth-service unreachable, SMTP down) is left
    // to propagate: KafkaErrorHandlingConfig's DefaultErrorHandler retries it a couple of
    // times, then routes it to task.assigned.DLT instead of retrying forever.
    @KafkaListener(topics = EventTopics.TASK_ASSIGNED)
    public void onTaskAssigned(TaskAssignedEvent event) {
        authUserClient.findEmail(event.assigneeUserId()).ifPresentOrElse(
                email -> emailService.send(email, "You've been assigned a task",
                        "You've been assigned a new task (id: " + event.taskId() + ")."),
                () -> log.warn("No user found for assigneeUserId {} on task {} - skipping notification",
                        event.assigneeUserId(), event.taskId()));
    }
}
