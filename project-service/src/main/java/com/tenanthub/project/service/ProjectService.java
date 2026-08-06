package com.tenanthub.project.service;

import com.tenanthub.project.entity.Project;
import com.tenanthub.project.exception.ResourceNotFoundException;
import com.tenanthub.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public Project createProject(UUID tenantId, String name, String description) {
        Project project = Project.builder()
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .build();
        return projectRepository.save(project);
    }

    // Not found and "belongs to another tenant" return the same 404 - confirming a
    // project exists in someone else's tenant is its own leak (see Task/CommentService).
    public Project getProject(UUID tenantId, UUID id) {
        return projectRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    public List<Project> listProjects(UUID tenantId) {
        return projectRepository.findByTenantId(tenantId);
    }

    @Transactional
    public Project updateProject(UUID tenantId, UUID id, String name, String description) {
        Project project = getProject(tenantId, id);
        project.setName(name);
        project.setDescription(description);
        return project;
    }

    @Transactional
    public void deleteProject(UUID tenantId, UUID id) {
        projectRepository.delete(getProject(tenantId, id));
    }
}
