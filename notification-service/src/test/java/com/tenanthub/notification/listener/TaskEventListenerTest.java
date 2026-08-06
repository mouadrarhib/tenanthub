package com.tenanthub.notification.listener;

import com.tenanthub.events.TaskAssignedEvent;
import com.tenanthub.notification.client.AuthUserClient;
import com.tenanthub.notification.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskEventListenerTest {

    @Mock
    private AuthUserClient authUserClient;
    @Mock
    private EmailService emailService;

    private TaskEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new TaskEventListener(authUserClient, emailService);
    }

    private TaskAssignedEvent event(UUID taskId, UUID assigneeUserId) {
        return new TaskAssignedEvent(taskId, UUID.randomUUID(), UUID.randomUUID(), assigneeUserId, Instant.now());
    }

    @Test
    void onTaskAssigned_userFound_sendsEmail() {
        UUID taskId = UUID.randomUUID();
        UUID assigneeUserId = UUID.randomUUID();
        when(authUserClient.findEmail(assigneeUserId)).thenReturn(Optional.of("jane@tenanthub.com"));

        listener.onTaskAssigned(event(taskId, assigneeUserId));

        verify(emailService).send(eq("jane@tenanthub.com"), anyString(), anyString());
    }

    @Test
    void onTaskAssigned_userNotFound_skipsEmail() {
        UUID assigneeUserId = UUID.randomUUID();
        when(authUserClient.findEmail(assigneeUserId)).thenReturn(Optional.empty());

        listener.onTaskAssigned(event(UUID.randomUUID(), assigneeUserId));

        verify(emailService, never()).send(any(), any(), any());
    }

    @Test
    void onTaskAssigned_lookupFails_propagatesForRetryAndDlt() {
        UUID assigneeUserId = UUID.randomUUID();
        when(authUserClient.findEmail(assigneeUserId)).thenThrow(new RuntimeException("auth-service unreachable"));
        TaskAssignedEvent event = event(UUID.randomUUID(), assigneeUserId);

        assertThatThrownBy(() -> listener.onTaskAssigned(event)).isInstanceOf(RuntimeException.class);

        verify(emailService, never()).send(any(), any(), any());
    }
}
