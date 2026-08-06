package com.tenanthub.tenant.dto;

import com.tenanthub.tenant.entity.Plan;

import java.util.UUID;

public record PlanResponse(
        UUID id,
        String name,
        int maxUsers,
        int maxProjects,
        int priceCents
) {

    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getMaxUsers(),
                plan.getMaxProjects(),
                plan.getPriceCents()
        );
    }
}
