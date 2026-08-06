package com.tenanthub.tenant.event;

import com.tenanthub.events.EventTopics;
import com.tenanthub.events.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTenantCreated(TenantCreatedEvent event) {
        kafkaTemplate.send(EventTopics.TENANT_CREATED, event.tenantId().toString(), event);
    }
}
