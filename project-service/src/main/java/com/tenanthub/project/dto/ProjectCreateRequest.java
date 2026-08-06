package com.tenanthub.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// tenantId is deliberately absent - it comes from the caller's JWT (TenantContext),
// never from the request body, so a client can't create a project in another tenant.
public record ProjectCreateRequest(
        @NotBlank @Size(max = 255) String name,
        String description
) {
}
