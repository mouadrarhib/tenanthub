package com.tenanthub.auth.controller;

import com.tenanthub.auth.entity.User;
import com.tenanthub.auth.exception.ResourceNotFoundException;
import com.tenanthub.auth.service.UserQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserLookupController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserLookupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserQueryService userQueryService;

    @Test
    void getUser_found_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).tenantId(UUID.randomUUID()).email("jane@tenanthub.com")
                .passwordHash("hash").build();
        when(userQueryService.getUser(userId)).thenReturn(user);

        mockMvc.perform(get("/internal/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("jane@tenanthub.com"));
    }

    @Test
    void getUser_notFound_returnsNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userQueryService.getUser(userId)).thenThrow(new ResourceNotFoundException("User not found: " + userId));

        mockMvc.perform(get("/internal/users/{id}", userId))
                .andExpect(status().isNotFound());
    }
}
