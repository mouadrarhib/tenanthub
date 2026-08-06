package com.tenanthub.billing.listener;

import com.tenanthub.billing.service.UsageTrackingService;
import com.tenanthub.events.TenantCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TenantEventListenerTest {

    @Mock
    private UsageTrackingService usageTrackingService;

    private TenantEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new TenantEventListener(usageTrackingService);
    }

    private TenantCreatedEvent event() {
        return new TenantCreatedEvent(UUID.randomUUID(), "Acme Inc", UUID.randomUUID(), "pro", 10, 20, Instant.now());
    }

    @Test
    void onTenantCreated_delegatesToUsageTrackingService() {
        TenantCreatedEvent event = event();

        listener.onTenantCreated(event);

        verify(usageTrackingService).recordTenantCreated(event);
    }

    @Test
    void onTenantCreated_serviceThrows_doesNotPropagate() {
        TenantCreatedEvent event = event();
        doThrow(new RuntimeException("db unavailable")).when(usageTrackingService).recordTenantCreated(event);

        listener.onTenantCreated(event);
    }
}
