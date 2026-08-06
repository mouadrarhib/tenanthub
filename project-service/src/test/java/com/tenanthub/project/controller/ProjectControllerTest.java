package com.tenanthub.project.controller;

import com.tenanthub.project.entity.Project;
import com.tenanthub.project.exception.ResourceNotFoundException;
import com.tenanthub.project.security.JwtTestSupport;
import com.tenanthub.project.service.ProjectService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller unit tests: MockMvc + a mocked ProjectService, no Spring context beyond
 * the web slice, no database. Security filters are disabled (addFilters = false) so
 * these tests stay scoped to the controller/validation layer - the jwt() request
 * post-processor stands in for the real filter chain just enough to populate
 * @AuthenticationPrincipal Jwt with a tenantId claim, since every endpoint reads the
 * caller's tenant from there rather than the request body.
 */
@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProject_returnsCreated() throws Exception {
        UUID tenantId = UUID.randomUUID();
        Project project = Project.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .name("Launch Website")
                .description("Q3 relaunch")
                .createdAt(LocalDateTime.now())
                .build();
        when(projectService.createProject(tenantId, "Launch Website", "Q3 relaunch")).thenReturn(project);

        mockMvc.perform(post("/api/projects")
                        .with(JwtTestSupport.withTenant(tenantId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Launch Website","description":"Q3 relaunch"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(project.getId().toString()))
                .andExpect(jsonPath("$.name").value("Launch Website"));

        verify(projectService).createProject(tenantId, "Launch Website", "Q3 relaunch");
    }

    @Test
    void createProject_blankName_returnsBadRequest() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(post("/api/projects")
                        .with(JwtTestSupport.withTenant(tenantId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","description":"desc"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listProjects_returnsOk() throws Exception {
        UUID tenantId = UUID.randomUUID();
        Project project = Project.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .name("Launch Website")
                .createdAt(LocalDateTime.now())
                .build();
        when(projectService.listProjects(tenantId)).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects")
                        .with(JwtTestSupport.withTenant(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(project.getId().toString()))
                .andExpect(jsonPath("$[0].name").value("Launch Website"));
    }

    @Test
    void getProject_found_returnsOk() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Project project = Project.builder()
                .id(id)
                .tenantId(tenantId)
                .name("Launch Website")
                .createdAt(LocalDateTime.now())
                .build();
        when(projectService.getProject(tenantId, id)).thenReturn(project);

        mockMvc.perform(get("/api/projects/{id}", id)
                        .with(JwtTestSupport.withTenant(tenantId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getProject_notFound_returnsNotFound() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        when(projectService.getProject(tenantId, id)).thenThrow(new ResourceNotFoundException("Project not found: " + id));

        mockMvc.perform(get("/api/projects/{id}", id)
                        .with(JwtTestSupport.withTenant(tenantId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Project not found: " + id))
                .andExpect(jsonPath("$.path").value("/api/projects/" + id));
    }

    @Test
    void updateProject_returnsOk() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Project updated = Project.builder()
                .id(id)
                .tenantId(tenantId)
                .name("New name")
                .description("New desc")
                .createdAt(LocalDateTime.now())
                .build();
        when(projectService.updateProject(tenantId, id, "New name", "New desc")).thenReturn(updated);

        mockMvc.perform(put("/api/projects/{id}", id)
                        .with(JwtTestSupport.withTenant(tenantId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New name","description":"New desc"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.description").value("New desc"));
    }

    @Test
    void updateProject_blankName_returnsBadRequest() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/projects/{id}", id)
                        .with(JwtTestSupport.withTenant(tenantId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","description":"desc"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteProject_returnsNoContent() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/projects/{id}", id)
                        .with(JwtTestSupport.withTenant(tenantId)))
                .andExpect(status().isNoContent());

        verify(projectService).deleteProject(tenantId, id);
    }
}
