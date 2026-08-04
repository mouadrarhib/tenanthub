package com.tenanthub.project.dto;

import com.tenanthub.project.entity.Task;
import com.tenanthub.project.entity.TaskStatus;

import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID projectId,
        String title,
        TaskStatus status,
        UUID assigneeUserId,
        LocalDate dueDate
) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                task.getStatus(),
                task.getAssigneeUserId(),
                task.getDueDate()
        );
    }
}
