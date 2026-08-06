package com.tenanthub.project.service;

import com.tenanthub.events.TaskAssignedEvent;
import com.tenanthub.events.TaskCreatedEvent;
import com.tenanthub.project.entity.Project;
import com.tenanthub.project.entity.Task;
import com.tenanthub.project.entity.TaskStatus;
import com.tenanthub.project.event.TaskEventPublisher;
import com.tenanthub.project.exception.ResourceNotFoundException;
import com.tenanthub.project.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final TaskEventPublisher eventPublisher;

    @Transactional
    public Task createTask(UUID tenantId, UUID projectId, String title, TaskStatus status, UUID assigneeUserId, LocalDate dueDate) {
        // Tenant-scoped lookup - a project id from another tenant 404s here rather
        // than silently attaching a task to a project the caller can't see.
        Project project = projectService.getProject(tenantId, projectId);
        Task task = Task.builder()
                .project(project)
                .title(title)
                .status(status != null ? status : TaskStatus.TODO)
                .assigneeUserId(assigneeUserId)
                .dueDate(dueDate)
                .build();
        Task saved = taskRepository.save(task);

        eventPublisher.publishTaskCreated(new TaskCreatedEvent(
                saved.getId(), projectId, tenantId, saved.getTitle(), saved.getAssigneeUserId(), Instant.now()));
        if (saved.getAssigneeUserId() != null) {
            eventPublisher.publishTaskAssigned(new TaskAssignedEvent(
                    saved.getId(), projectId, tenantId, saved.getAssigneeUserId(), Instant.now()));
        }
        return saved;
    }

    public Task getTask(UUID tenantId, UUID id) {
        return taskRepository.findByIdAndProject_TenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    public List<Task> listTasksByProject(UUID tenantId, UUID projectId) {
        projectService.getProject(tenantId, projectId);
        return taskRepository.findByProjectId(projectId);
    }

    @Transactional
    public Task updateTask(UUID tenantId, UUID id, String title, TaskStatus status, UUID assigneeUserId, LocalDate dueDate) {
        Task task = getTask(tenantId, id);
        UUID previousAssignee = task.getAssigneeUserId();
        task.setTitle(title);
        task.setStatus(status);
        task.setAssigneeUserId(assigneeUserId);
        task.setDueDate(dueDate);

        if (assigneeUserId != null && !assigneeUserId.equals(previousAssignee)) {
            eventPublisher.publishTaskAssigned(new TaskAssignedEvent(
                    task.getId(), task.getProject().getId(), tenantId, assigneeUserId, Instant.now()));
        }
        return task;
    }

    @Transactional
    public void deleteTask(UUID tenantId, UUID id) {
        taskRepository.delete(getTask(tenantId, id));
    }
}
