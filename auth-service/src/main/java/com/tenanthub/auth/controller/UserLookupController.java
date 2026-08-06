package com.tenanthub.auth.controller;

import com.tenanthub.auth.dto.UserSummaryResponse;
import com.tenanthub.auth.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// Server-to-server only, not part of the public auth API - other services resolve a
// userId to an email/tenantId here instead of reaching into auth_db directly. No
// Gateway/service-auth exists yet (that's P5), so this is trusted-network-only for now.
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserLookupController {

    private final UserQueryService userQueryService;

    @GetMapping("/{id}")
    public UserSummaryResponse getUser(@PathVariable UUID id) {
        var user = userQueryService.getUser(id);
        return new UserSummaryResponse(user.getId(), user.getEmail());
    }
}
