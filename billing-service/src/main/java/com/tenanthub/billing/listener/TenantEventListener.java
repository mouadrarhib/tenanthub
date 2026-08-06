package com.tenanthub.billing.listener;

import com.tenanthub.billing.service.UsageTrackingService;
import com.tenanthub.events.EventTopics;
import com.tenanthub.events.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantEventListener {

    private final UsageTrackingService usageTrackingService;

    // Broad catch is deliberate - a bad/unreachable event should be logged and
    // skipped, not left to Spring Kafka's default retry behavior, until the
    // dead-letter topic step formalizes that (see notification-service's
    // TaskEventListener for the same pattern).
    @KafkaListener(topics = EventTopics.TENANT_CREATED)
    public void onTenantCreated(TenantCreatedEvent event) {
        try {
            usageTrackingService.recordTenantCreated(event);
        } catch (Exception e) {
            log.error("Failed to process tenant.created event for tenant {}", event.tenantId(), e);
        }
    }
}
