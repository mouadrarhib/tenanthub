package com.tenanthub.billing.listener;

import com.tenanthub.billing.service.UsageTrackingService;
import com.tenanthub.events.EventTopics;
import com.tenanthub.events.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantEventListener {

    private final UsageTrackingService usageTrackingService;

    // Exceptions propagate on purpose - KafkaErrorHandlingConfig's DefaultErrorHandler
    // retries a couple of times, then routes the record to tenant.created.DLT instead
    // of retrying forever or silently dropping it.
    @KafkaListener(topics = EventTopics.TENANT_CREATED)
    public void onTenantCreated(TenantCreatedEvent event) {
        usageTrackingService.recordTenantCreated(event);
    }
}
