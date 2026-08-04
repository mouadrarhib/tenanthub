package com.tenanthub.project.dto;

import com.tenanthub.project.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record TaskRequest(
        @NotBlank @Size(max = 255) String title,
        TaskStatus status,
        UUID assigneeUserId,
        LocalDate dueDate
) {
}
