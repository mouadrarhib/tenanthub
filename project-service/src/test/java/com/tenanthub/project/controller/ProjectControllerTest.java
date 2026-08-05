package com.tenanthub.project.controller;

import com.tenanthub.project.entity.Project;
import com.tenanthub.project.exception.ResourceNotFoundException;
import com.tenanthub.project.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
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
 * the web slice, no database. Security filters are disabled here so these tests stay
 * scoped to the controller/validation layer, independent of the JWT auth wired up in
 * security/SecurityConfig.
 */
@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tenantId":"%s","name":"Launch Website","description":"Q3 relaunch"}
                                """.formatted(tenantId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(project.getId().toString()))
                .andExpect(jsonPath("$.name").value("Launch Website"));

        verify(projectService).createProject(tenantId, "Launch Website", "Q3 relaunch");
    }

    @Test
    void createProject_blankName_returnsBadRequest() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tenantId":"%s","name":"","description":"desc"}
                                """.formatted(tenantId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProject_missingTenantId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Launch Website"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listProjects_returnsOk() throws Exception {
        Project project = Project.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .name("Launch Website")
                .createdAt(LocalDateTime.now())
                .build();
        when(projectService.listProjects()).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(project.getId().toString()))
                .andExpect(jsonPath("$[0].name").value("Launch Website"));
    }

    @Test
    void getProject_found_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        Project project = Project.builder()
                .id(id)
                .tenantId(UUID.randomUUID())
                .name("Launch Website")
                .createdAt(LocalDateTime.now())
                .build();
        when(projectService.getProject(id)).thenReturn(project);

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getProject_notFound_returnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(projectService.getProject(id)).thenThrow(new ResourceNotFoundException("Project not found: " + id));

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Project not found: " + id))
                .andExpect(jsonPath("$.path").value("/api/projects/" + id));
    }

    @Test
    void updateProject_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        Project updated = Project.builder()
                .id(id)
                .tenantId(UUID.randomUUID())
                .name("New name")
                .description("New desc")
                .createdAt(LocalDateTime.now())
                .build();
        when(projectService.updateProject(id, "New name", "New desc")).thenReturn(updated);

        mockMvc.perform(put("/api/projects/{id}", id)
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
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/projects/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","description":"desc"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteProject_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/projects/{id}", id))
                .andExpect(status().isNoContent());

        verify(projectService).deleteProject(id);
    }
}
