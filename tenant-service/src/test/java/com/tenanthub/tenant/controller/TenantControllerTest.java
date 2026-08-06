package com.tenanthub.tenant.controller;

import com.tenanthub.tenant.entity.Plan;
import com.tenanthub.tenant.entity.Tenant;
import com.tenanthub.tenant.exception.ResourceNotFoundException;
import com.tenanthub.tenant.service.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller unit tests: MockMvc + a mocked TenantService, no Spring context beyond
 * the web slice, no database. tenant-service has no Spring Security on the classpath -
 * signup is a public endpoint (there's no JWT to gate it with until Auth Service issues
 * one against the tenant this call creates) - so unlike project-service's controller
 * tests, there's no JWT/principal to stand up here.
 */
@WebMvcTest(TenantController.class)
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TenantService tenantService;

    private Tenant tenantWith(UUID id, UUID planId, String name) {
        Plan plan = Plan.builder().id(planId).name("free").maxUsers(5).maxProjects(3).priceCents(0).build();
        return Tenant.builder()
                .id(id)
                .name(name)
                .plan(plan)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void signUp_returnsCreated() throws Exception {
        UUID planId = UUID.randomUUID();
        Tenant tenant = tenantWith(UUID.randomUUID(), planId, "Acme Corp");
        when(tenantService.signUp("Acme Corp", planId)).thenReturn(tenant);

        mockMvc.perform(post("/api/tenants/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Acme Corp","planId":"%s"}
                                """.formatted(planId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Acme Corp"))
                .andExpect(jsonPath("$.planId").value(planId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void signUp_blankName_returnsBadRequest() throws Exception {
        UUID planId = UUID.randomUUID();

        mockMvc.perform(post("/api/tenants/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","planId":"%s"}
                                """.formatted(planId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signUp_missingPlanId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tenants/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Acme Corp"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signUp_planNotFound_returnsNotFound() throws Exception {
        UUID planId = UUID.randomUUID();
        when(tenantService.signUp("Acme Corp", planId))
                .thenThrow(new ResourceNotFoundException("Plan not found: " + planId));

        mockMvc.perform(post("/api/tenants/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Acme Corp","planId":"%s"}
                                """.formatted(planId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Plan not found: " + planId));
    }

    @Test
    void getTenant_found_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        Tenant tenant = tenantWith(id, UUID.randomUUID(), "Acme Corp");
        when(tenantService.getTenant(id)).thenReturn(tenant);

        mockMvc.perform(get("/api/tenants/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Acme Corp"));
    }

    @Test
    void getTenant_notFound_returnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(tenantService.getTenant(id)).thenThrow(new ResourceNotFoundException("Tenant not found: " + id));

        mockMvc.perform(get("/api/tenants/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Tenant not found: " + id))
                .andExpect(jsonPath("$.path").value("/api/tenants/" + id));
    }
}
