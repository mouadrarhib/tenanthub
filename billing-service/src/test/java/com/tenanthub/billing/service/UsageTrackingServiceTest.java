package com.tenanthub.billing.service;

import com.tenanthub.billing.entity.TenantPlanLimits;
import com.tenanthub.billing.entity.UsageRecord;
import com.tenanthub.billing.repository.TenantPlanLimitsRepository;
import com.tenanthub.billing.repository.UsageRecordRepository;
import com.tenanthub.events.ProjectCreatedEvent;
import com.tenanthub.events.TenantCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsageTrackingServiceTest {

    @Mock
    private TenantPlanLimitsRepository tenantPlanLimitsRepository;
    @Mock
    private UsageRecordRepository usageRecordRepository;

    private UsageTrackingService serviceUnderTest() {
        return new UsageTrackingService(tenantPlanLimitsRepository, usageRecordRepository);
    }

    @Test
    void recordTenantCreated_savesPlanLimits() {
        UsageTrackingService usageTrackingService = serviceUnderTest();
        UUID tenantId = UUID.randomUUID();
        TenantCreatedEvent event = new TenantCreatedEvent(
                tenantId, "Acme Inc", UUID.randomUUID(), "pro", 10, 20, Instant.now());

        usageTrackingService.recordTenantCreated(event);

        ArgumentCaptor<TenantPlanLimits> captor = ArgumentCaptor.forClass(TenantPlanLimits.class);
        verify(tenantPlanLimitsRepository).save(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(tenantId);
        assertThat(captor.getValue().getPlanName()).isEqualTo("pro");
        assertThat(captor.getValue().getMaxUsers()).isEqualTo(10);
        assertThat(captor.getValue().getMaxProjects()).isEqualTo(20);
    }

    @Test
    void recordProjectCreated_underLimit_savesUsageRecordWithoutWarning() {
        UsageTrackingService usageTrackingService = serviceUnderTest();
        UUID tenantId = UUID.randomUUID();
        ProjectCreatedEvent event = new ProjectCreatedEvent(UUID.randomUUID(), tenantId, "Launch Website", Instant.now());
        when(usageRecordRepository.countByTenantIdAndMetricName(tenantId, "projects_count")).thenReturn(1L);
        when(tenantPlanLimitsRepository.findById(tenantId)).thenReturn(
                Optional.of(TenantPlanLimits.builder().tenantId(tenantId).planName("pro").maxUsers(10).maxProjects(20).build()));

        usageTrackingService.recordProjectCreated(event);

        ArgumentCaptor<UsageRecord> captor = ArgumentCaptor.forClass(UsageRecord.class);
        verify(usageRecordRepository).save(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(tenantId);
        assertThat(captor.getValue().getMetricName()).isEqualTo("projects_count");
        assertThat(captor.getValue().getValue()).isEqualTo(1);
    }

    @Test
    void recordProjectCreated_noPlanLimitsCached_doesNotThrow() {
        UsageTrackingService usageTrackingService = serviceUnderTest();
        UUID tenantId = UUID.randomUUID();
        ProjectCreatedEvent event = new ProjectCreatedEvent(UUID.randomUUID(), tenantId, "Launch Website", Instant.now());
        when(usageRecordRepository.countByTenantIdAndMetricName(any(), any())).thenReturn(1L);
        when(tenantPlanLimitsRepository.findById(tenantId)).thenReturn(Optional.empty());

        usageTrackingService.recordProjectCreated(event);

        verify(usageRecordRepository).save(any(UsageRecord.class));
    }

    @Test
    void recordProjectCreated_overLimit_stillSavesUsageRecord() {
        UsageTrackingService usageTrackingService = serviceUnderTest();
        UUID tenantId = UUID.randomUUID();
        ProjectCreatedEvent event = new ProjectCreatedEvent(UUID.randomUUID(), tenantId, "Launch Website", Instant.now());
        when(usageRecordRepository.countByTenantIdAndMetricName(tenantId, "projects_count")).thenReturn(21L);
        when(tenantPlanLimitsRepository.findById(tenantId)).thenReturn(
                Optional.of(TenantPlanLimits.builder().tenantId(tenantId).planName("pro").maxUsers(10).maxProjects(20).build()));

        usageTrackingService.recordProjectCreated(event);

        verify(usageRecordRepository).save(any(UsageRecord.class));
    }
}
