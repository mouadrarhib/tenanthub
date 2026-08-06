package com.tenanthub.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TenantSignupRequest(
        @NotBlank String name,
        @NotNull UUID planId
) {
}
