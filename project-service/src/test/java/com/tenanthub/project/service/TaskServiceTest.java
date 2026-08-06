package com.tenanthub.project.service;

import com.tenanthub.events.TaskAssignedEvent;
import com.tenanthub.events.TaskCreatedEvent;
import com.tenanthub.project.entity.Project;
import com.tenanthub.project.entity.Task;
import com.tenanthub.project.entity.TaskStatus;
import com.tenanthub.project.event.TaskEventPublisher;
import com.tenanthub.project.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private TaskEventPublisher eventPublisher;

    private TaskService taskService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID projectId = UUID.randomUUID();

    @Test
    void createTask_withoutAssignee_publishesOnlyTaskCreated() {
        taskService = new TaskService(taskRepository, projectService, eventPublisher);
        Project project = Project.builder().id(projectId).tenantId(tenantId).build();
        when(projectService.getProject(tenantId, projectId)).thenReturn(project);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(UUID.randomUUID());
            return task;
        });

        Task created = taskService.createTask(tenantId, projectId, "Design homepage", null, null, null);

        ArgumentCaptor<TaskCreatedEvent> captor = ArgumentCaptor.forClass(TaskCreatedEvent.class);
        verify(eventPublisher).publishTaskCreated(captor.capture());
        assertThat(captor.getValue().taskId()).isEqualTo(created.getId());
        assertThat(captor.getValue().projectId()).isEqualTo(projectId);
        assertThat(captor.getValue().tenantId()).isEqualTo(tenantId);
        verify(eventPublisher, never()).publishTaskAssigned(any());
    }

    @Test
    void createTask_withAssignee_alsoPublishesTaskAssigned() {
        taskService = new TaskService(taskRepository, projectService, eventPublisher);
        UUID assignee = UUID.randomUUID();
        Project project = Project.builder().id(projectId).tenantId(tenantId).build();
        when(projectService.getProject(tenantId, projectId)).thenReturn(project);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(UUID.randomUUID());
            return task;
        });

        Task created = taskService.createTask(tenantId, projectId, "Design homepage", null, assignee, null);

        ArgumentCaptor<TaskAssignedEvent> captor = ArgumentCaptor.forClass(TaskAssignedEvent.class);
        verify(eventPublisher).publishTaskAssigned(captor.capture());
        assertThat(captor.getValue().taskId()).isEqualTo(created.getId());
        assertThat(captor.getValue().assigneeUserId()).isEqualTo(assignee);
    }

    @Test
    void updateTask_assigneeChanged_publishesTaskAssigned() {
        taskService = new TaskService(taskRepository, projectService, eventPublisher);
        UUID taskId = UUID.randomUUID();
        UUID newAssignee = UUID.randomUUID();
        Project project = Project.builder().id(projectId).tenantId(tenantId).build();
        Task existing = Task.builder().id(taskId).project(project).title("Old title")
                .status(TaskStatus.TODO).build();
        when(taskRepository.findByIdAndProject_TenantId(taskId, tenantId)).thenReturn(Optional.of(existing));

        taskService.updateTask(tenantId, taskId, "Old title", TaskStatus.DOING, newAssignee, null);

        ArgumentCaptor<TaskAssignedEvent> captor = ArgumentCaptor.forClass(TaskAssignedEvent.class);
        verify(eventPublisher).publishTaskAssigned(captor.capture());
        assertThat(captor.getValue().taskId()).isEqualTo(taskId);
        assertThat(captor.getValue().assigneeUserId()).isEqualTo(newAssignee);
    }

    @Test
    void updateTask_assigneeUnchanged_doesNotPublishTaskAssigned() {
        taskService = new TaskService(taskRepository, projectService, eventPublisher);
        UUID taskId = UUID.randomUUID();
        UUID assignee = UUID.randomUUID();
        Project project = Project.builder().id(projectId).tenantId(tenantId).build();
        Task existing = Task.builder().id(taskId).project(project).title("Old title")
                .status(TaskStatus.TODO).assigneeUserId(assignee).build();
        when(taskRepository.findByIdAndProject_TenantId(taskId, tenantId)).thenReturn(Optional.of(existing));

        taskService.updateTask(tenantId, taskId, "New title", TaskStatus.DOING, assignee, null);

        verify(eventPublisher, never()).publishTaskAssigned(any());
    }
}
