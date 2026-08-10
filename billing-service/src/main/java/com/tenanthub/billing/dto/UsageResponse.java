package com.tenanthub.billing.dto;

import com.tenanthub.billing.entity.TenantPlanLimits;

import java.util.UUID;

public record UsageResponse(
        UUID tenantId,
        String planName,
        int maxUsers,
        int maxProjects,
        long projectsUsed
) {

    public static UsageResponse of(TenantPlanLimits limits, long projectsUsed) {
        return new UsageResponse(
                limits.getTenantId(),
                limits.getPlanName(),
                limits.getMaxUsers(),
                limits.getMaxProjects(),
                projectsUsed
        );
    }
}
