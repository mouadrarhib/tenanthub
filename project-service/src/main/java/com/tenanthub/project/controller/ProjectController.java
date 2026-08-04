package com.tenanthub.project.controller;

import com.tenanthub.project.dto.ProjectCreateRequest;
import com.tenanthub.project.dto.ProjectResponse;
import com.tenanthub.project.dto.ProjectUpdateRequest;
import com.tenanthub.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody ProjectCreateRequest request) {
        return ProjectResponse.from(
                projectService.createProject(request.tenantId(), request.name(), request.description())
        );
    }

    @GetMapping
    public List<ProjectResponse> listProjects() {
        return projectService.listProjects().stream().map(ProjectResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProjectResponse getProject(@PathVariable UUID id) {
        return ProjectResponse.from(projectService.getProject(id));
    }

    @PutMapping("/{id}")
    public ProjectResponse updateProject(@PathVariable UUID id, @Valid @RequestBody ProjectUpdateRequest request) {
        return ProjectResponse.from(
                projectService.updateProject(id, request.name(), request.description())
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
    }
}
