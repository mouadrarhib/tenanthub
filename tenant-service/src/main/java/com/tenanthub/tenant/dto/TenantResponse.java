package com.tenanthub.tenant.dto;

import com.tenanthub.tenant.entity.Tenant;
import com.tenanthub.tenant.entity.TenantStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        UUID planId,
        String planName,
        TenantStatus status,
        LocalDateTime createdAt
) {

    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getPlan().getId(),
                tenant.getPlan().getName(),
                tenant.getStatus(),
                tenant.getCreatedAt()
        );
    }
}
