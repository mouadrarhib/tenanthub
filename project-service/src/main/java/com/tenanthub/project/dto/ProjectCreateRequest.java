package com.tenanthub.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProjectCreateRequest(
        @NotNull UUID tenantId,
        @NotBlank @Size(max = 255) String name,
        String description
) {
}
