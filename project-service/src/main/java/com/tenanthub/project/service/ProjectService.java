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

    public Project getProject(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    public List<Project> listProjects() {
        return projectRepository.findAll();
    }

    @Transactional
    public Project updateProject(UUID id, String name, String description) {
        Project project = getProject(id);
        project.setName(name);
        project.setDescription(description);
        return project;
    }

    @Transactional
    public void deleteProject(UUID id) {
        projectRepository.delete(getProject(id));
    }
}
