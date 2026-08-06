package com.tenanthub.tenant.service;

import com.tenanthub.events.TenantCreatedEvent;
import com.tenanthub.tenant.entity.Plan;
import com.tenanthub.tenant.entity.Tenant;
import com.tenanthub.tenant.event.TenantEventPublisher;
import com.tenanthub.tenant.exception.ResourceNotFoundException;
import com.tenanthub.tenant.repository.PlanRepository;
import com.tenanthub.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;
    private final TenantEventPublisher eventPublisher;

    @Transactional
    public Tenant signUp(String name, UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        Tenant tenant = Tenant.builder()
                .name(name)
                .plan(plan)
                .build();
        Tenant saved = tenantRepository.save(tenant);

        eventPublisher.publishTenantCreated(new TenantCreatedEvent(
                saved.getId(), saved.getName(), plan.getId(), plan.getName(),
                plan.getMaxUsers(), plan.getMaxProjects(), Instant.now()));
        return saved;
    }

    public Tenant getTenant(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + id));
    }
}
