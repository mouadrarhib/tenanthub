package com.tenanthub.tenant.service;

import com.tenanthub.tenant.entity.Plan;
import com.tenanthub.tenant.entity.Tenant;
import com.tenanthub.tenant.exception.ResourceNotFoundException;
import com.tenanthub.tenant.repository.PlanRepository;
import com.tenanthub.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;

    @Transactional
    public Tenant signUp(String name, UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        Tenant tenant = Tenant.builder()
                .name(name)
                .plan(plan)
                .build();
        return tenantRepository.save(tenant);
    }

    public Tenant getTenant(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + id));
    }
}
