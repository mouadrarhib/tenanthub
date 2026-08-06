package com.tenanthub.project.service;

import com.tenanthub.events.ProjectCreatedEvent;
import com.tenanthub.project.entity.Project;
import com.tenanthub.project.event.ProjectEventPublisher;
import com.tenanthub.project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectEventPublisher eventPublisher;

    @Test
    void createProject_publishesProjectCreated() {
        ProjectService projectService = new ProjectService(projectRepository, eventPublisher);
        UUID tenantId = UUID.randomUUID();
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(UUID.randomUUID());
            return project;
        });

        Project created = projectService.createProject(tenantId, "Launch Website", "Q3 relaunch");

        ArgumentCaptor<ProjectCreatedEvent> captor = ArgumentCaptor.forClass(ProjectCreatedEvent.class);
        verify(eventPublisher).publishProjectCreated(captor.capture());
        assertThat(captor.getValue().projectId()).isEqualTo(created.getId());
        assertThat(captor.getValue().tenantId()).isEqualTo(tenantId);
        assertThat(captor.getValue().name()).isEqualTo("Launch Website");
    }
}
