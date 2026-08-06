package com.tenanthub.tenant.service;

import com.tenanthub.events.TenantCreatedEvent;
import com.tenanthub.tenant.entity.Plan;
import com.tenanthub.tenant.entity.Tenant;
import com.tenanthub.tenant.event.TenantEventPublisher;
import com.tenanthub.tenant.repository.PlanRepository;
import com.tenanthub.tenant.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private PlanRepository planRepository;
    @Mock
    private TenantEventPublisher eventPublisher;

    @Test
    void signUp_publishesTenantCreatedWithPlanLimits() {
        TenantService tenantService = new TenantService(tenantRepository, planRepository, eventPublisher);
        UUID planId = UUID.randomUUID();
        Plan plan = Plan.builder().id(planId).name("pro").maxUsers(10).maxProjects(20).priceCents(4900).build();
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant tenant = invocation.getArgument(0);
            tenant.setId(UUID.randomUUID());
            return tenant;
        });

        Tenant saved = tenantService.signUp("Acme Inc", planId);

        ArgumentCaptor<TenantCreatedEvent> captor = ArgumentCaptor.forClass(TenantCreatedEvent.class);
        verify(eventPublisher).publishTenantCreated(captor.capture());
        TenantCreatedEvent event = captor.getValue();
        assertThat(event.tenantId()).isEqualTo(saved.getId());
        assertThat(event.name()).isEqualTo("Acme Inc");
        assertThat(event.planId()).isEqualTo(planId);
        assertThat(event.planName()).isEqualTo("pro");
        assertThat(event.maxUsers()).isEqualTo(10);
        assertThat(event.maxProjects()).isEqualTo(20);
    }
}
