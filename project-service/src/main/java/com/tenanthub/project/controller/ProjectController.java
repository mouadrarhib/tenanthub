package com.tenanthub.project.controller;

import com.tenanthub.project.dto.ProjectCreateRequest;
import com.tenanthub.project.dto.ProjectResponse;
import com.tenanthub.project.dto.ProjectUpdateRequest;
import com.tenanthub.project.security.TenantContext;
import com.tenanthub.project.service.ProjectService;
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
    public ProjectResponse createProject(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProjectCreateRequest request) {
        return ProjectResponse.from(
                projectService.createProject(TenantContext.tenantId(jwt), request.name(), request.description())
        );
    }

    @GetMapping
    public List<ProjectResponse> listProjects(@AuthenticationPrincipal Jwt jwt) {
        return projectService.listProjects(TenantContext.tenantId(jwt)).stream().map(ProjectResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProjectResponse getProject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return ProjectResponse.from(projectService.getProject(TenantContext.tenantId(jwt), id));
    }

    @PutMapping("/{id}")
    public ProjectResponse updateProject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                          @Valid @RequestBody ProjectUpdateRequest request) {
        return ProjectResponse.from(
                projectService.updateProject(TenantContext.tenantId(jwt), id, request.name(), request.description())
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        projectService.deleteProject(TenantContext.tenantId(jwt), id);
    }
}
