package com.tenanthub.project.dto;

import com.tenanthub.project.entity.Project;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID tenantId,
        String name,
        String description,
        LocalDateTime createdAt
) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getTenantId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt()
        );
    }
}
