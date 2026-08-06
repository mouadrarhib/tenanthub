package com.tenanthub.billing.service;

import com.tenanthub.billing.entity.TenantPlanLimits;
import com.tenanthub.billing.entity.UsageRecord;
import com.tenanthub.billing.repository.TenantPlanLimitsRepository;
import com.tenanthub.billing.repository.UsageRecordRepository;
import com.tenanthub.events.ProjectCreatedEvent;
import com.tenanthub.events.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsageTrackingService {

    private static final String PROJECTS_COUNT_METRIC = "projects_count";

    private final TenantPlanLimitsRepository tenantPlanLimitsRepository;
    private final UsageRecordRepository usageRecordRepository;

    public void recordTenantCreated(TenantCreatedEvent event) {
        // tenantId is the @Id, so save() upserts - a re-delivered event just overwrites
        // with the same values instead of failing on a duplicate key.
        tenantPlanLimitsRepository.save(TenantPlanLimits.builder()
                .tenantId(event.tenantId())
                .planName(event.planName())
                .maxUsers(event.maxUsers())
                .maxProjects(event.maxProjects())
                .build());
    }

    public void recordProjectCreated(ProjectCreatedEvent event) {
        usageRecordRepository.save(UsageRecord.builder()
                .tenantId(event.tenantId())
                .metricName(PROJECTS_COUNT_METRIC)
                .value(1)
                .build());

        long currentCount = usageRecordRepository.countByTenantIdAndMetricName(event.tenantId(), PROJECTS_COUNT_METRIC);
        tenantPlanLimitsRepository.findById(event.tenantId()).ifPresentOrElse(
                limits -> {
                    if (currentCount > limits.getMaxProjects()) {
                        log.warn("Tenant {} has exceeded their plan's project limit ({}/{})",
                                event.tenantId(), currentCount, limits.getMaxProjects());
                    }
                },
                () -> log.warn("No plan limits cached for tenant {} - was tenant.created ever consumed?",
                        event.tenantId()));
    }
}
