package com.tenanthub.project.controller;

import com.tenanthub.project.entity.Project;
import com.tenanthub.project.entity.Task;
import com.tenanthub.project.entity.TaskStatus;
import com.tenanthub.project.exception.ResourceNotFoundException;
import com.tenanthub.project.service.TaskService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private Task taskFor(UUID projectId, UUID taskId, String title, TaskStatus status) {
        return Task.builder()
                .id(taskId)
                .project(Project.builder().id(projectId).build())
                .title(title)
                .status(status)
                .build();
    }

    @Test
    void createTask_returnsCreated() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID assignee = UUID.randomUUID();
        LocalDate dueDate = LocalDate.of(2026, 9, 1);
        Task task = taskFor(projectId, UUID.randomUUID(), "Design homepage", TaskStatus.TODO);
        task.setAssigneeUserId(assignee);
        task.setDueDate(dueDate);

        when(taskService.createTask(projectId, "Design homepage", null, assignee, dueDate)).thenReturn(task);

        mockMvc.perform(post("/api/projects/{projectId}/tasks", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Design homepage","assigneeUserId":"%s","dueDate":"2026-09-01"}
                                """.formatted(assignee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Design homepage"))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void createTask_blankTitle_returnsBadRequest() throws Exception {
        UUID projectId = UUID.randomUUID();

        mockMvc.perform(post("/api/projects/{projectId}/tasks", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_projectNotFound_currentlyPropagatesUnhandled() {
        // No @ControllerAdvice yet - ResourceNotFoundException isn't mapped to a response
        // status, so MockMvc rethrows it wrapped in a ServletException.
        UUID projectId = UUID.randomUUID();
        when(taskService.createTask(eq(projectId), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Project not found: " + projectId));

        ServletException exception = assertThrows(ServletException.class, () -> mockMvc.perform(
                post("/api/projects/{projectId}/tasks", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Ghost task"}
                                """)));

        assertThat(exception.getCause()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listTasks_returnsOk() throws Exception {
        UUID projectId = UUID.randomUUID();
        Task task = taskFor(projectId, UUID.randomUUID(), "Design homepage", TaskStatus.TODO);
        when(taskService.listTasksByProject(projectId)).thenReturn(List.of(task));

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Design homepage"));
    }

    @Test
    void getTask_found_returnsOk() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Task task = taskFor(projectId, taskId, "Design homepage", TaskStatus.TODO);
        when(taskService.getTask(taskId)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()));
    }

    @Test
    void getTask_notFound_currentlyPropagatesUnhandled() {
        UUID taskId = UUID.randomUUID();
        when(taskService.getTask(taskId)).thenThrow(new ResourceNotFoundException("Task not found: " + taskId));

        ServletException exception = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/api/tasks/{id}", taskId)));

        assertThat(exception.getCause()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTask_returnsOk() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Task updated = taskFor(projectId, taskId, "Design homepage", TaskStatus.DOING);

        when(taskService.updateTask(eq(taskId), eq("Design homepage"), eq(TaskStatus.DOING), isNull(), isNull()))
                .thenReturn(updated);

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Design homepage","status":"DOING"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOING"));
    }

    @Test
    void updateTask_blankTitle_returnsBadRequest() throws Exception {
        UUID taskId = UUID.randomUUID();

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTask_returnsNoContent() throws Exception {
        UUID taskId = UUID.randomUUID();

        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(taskId);
    }
}
