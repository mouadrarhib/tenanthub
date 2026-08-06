package com.tenanthub.billing.listener;

import com.tenanthub.billing.service.UsageTrackingService;
import com.tenanthub.events.ProjectCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectEventListenerTest {

    @Mock
    private UsageTrackingService usageTrackingService;

    private ProjectEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new ProjectEventListener(usageTrackingService);
    }

    private ProjectCreatedEvent event() {
        return new ProjectCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), "Launch Website", Instant.now());
    }

    @Test
    void onProjectCreated_delegatesToUsageTrackingService() {
        ProjectCreatedEvent event = event();

        listener.onProjectCreated(event);

        verify(usageTrackingService).recordProjectCreated(event);
    }

    @Test
    void onProjectCreated_serviceThrows_propagatesForRetryAndDlt() {
        ProjectCreatedEvent event = event();
        doThrow(new RuntimeException("db unavailable")).when(usageTrackingService).recordProjectCreated(event);

        assertThatThrownBy(() -> listener.onProjectCreated(event)).isInstanceOf(RuntimeException.class);
    }
}
