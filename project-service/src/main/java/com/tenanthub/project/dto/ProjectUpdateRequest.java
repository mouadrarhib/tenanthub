package com.tenanthub.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectUpdateRequest(
        @NotBlank @Size(max = 255) String name,
        String description
) {
}
