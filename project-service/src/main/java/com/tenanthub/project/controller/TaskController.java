package com.tenanthub.project.controller;

import com.tenanthub.project.dto.TaskRequest;
import com.tenanthub.project.dto.TaskResponse;
import com.tenanthub.project.security.TenantContext;
import com.tenanthub.project.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/api/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId,
                                    @Valid @RequestBody TaskRequest request) {
        return TaskResponse.from(
                taskService.createTask(TenantContext.tenantId(jwt), projectId, request.title(), request.status(),
                        request.assigneeUserId(), request.dueDate())
        );
    }

    @GetMapping("/api/projects/{projectId}/tasks")
    public List<TaskResponse> listTasks(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID projectId) {
        return taskService.listTasksByProject(TenantContext.tenantId(jwt), projectId).stream()
                .map(TaskResponse::from).toList();
    }

    @GetMapping("/api/tasks/{id}")
    public TaskResponse getTask(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return TaskResponse.from(taskService.getTask(TenantContext.tenantId(jwt), id));
    }

    @PutMapping("/api/tasks/{id}")
    public TaskResponse updateTask(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                    @Valid @RequestBody TaskRequest request) {
        return TaskResponse.from(
                taskService.updateTask(TenantContext.tenantId(jwt), id, request.title(), request.status(),
                        request.assigneeUserId(), request.dueDate())
        );
    }

    @DeleteMapping("/api/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        taskService.deleteTask(TenantContext.tenantId(jwt), id);
    }
}
