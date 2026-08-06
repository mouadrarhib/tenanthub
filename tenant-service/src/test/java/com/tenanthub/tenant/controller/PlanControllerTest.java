package com.tenanthub.tenant.controller;

import com.tenanthub.tenant.entity.Plan;
import com.tenanthub.tenant.service.PlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanController.class)
class PlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlanService planService;

    @Test
    void listPlans_returnsOk() throws Exception {
        Plan plan = Plan.builder()
                .id(UUID.randomUUID())
                .name("free")
                .maxUsers(5)
                .maxProjects(3)
                .priceCents(0)
                .build();
        when(planService.listPlans()).thenReturn(List.of(plan));

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("free"))
                .andExpect(jsonPath("$[0].maxUsers").value(5));
    }

    @Test
    void listPlans_empty_returnsOk() throws Exception {
        when(planService.listPlans()).thenReturn(List.of());

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
