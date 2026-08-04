package com.tenanthub.project.controller;

import com.tenanthub.project.dto.TaskRequest;
import com.tenanthub.project.dto.TaskResponse;
import com.tenanthub.project.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public TaskResponse createTask(@PathVariable UUID projectId, @Valid @RequestBody TaskRequest request) {
        return TaskResponse.from(
                taskService.createTask(projectId, request.title(), request.status(), request.assigneeUserId(), request.dueDate())
        );
    }

    @GetMapping("/api/projects/{projectId}/tasks")
    public List<TaskResponse> listTasks(@PathVariable UUID projectId) {
        return taskService.listTasksByProject(projectId).stream().map(TaskResponse::from).toList();
    }

    @GetMapping("/api/tasks/{id}")
    public TaskResponse getTask(@PathVariable UUID id) {
        return TaskResponse.from(taskService.getTask(id));
    }

    @PutMapping("/api/tasks/{id}")
    public TaskResponse updateTask(@PathVariable UUID id, @Valid @RequestBody TaskRequest request) {
        return TaskResponse.from(
                taskService.updateTask(id, request.title(), request.status(), request.assigneeUserId(), request.dueDate())
        );
    }

    @DeleteMapping("/api/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
    }
}
