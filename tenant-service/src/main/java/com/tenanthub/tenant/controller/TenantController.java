package com.tenanthub.tenant.controller;

import com.tenanthub.tenant.dto.TenantResponse;
import com.tenanthub.tenant.dto.TenantSignupRequest;
import com.tenanthub.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public TenantResponse signUp(@Valid @RequestBody TenantSignupRequest request) {
        return TenantResponse.from(tenantService.signUp(request.name(), request.planId()));
    }

    @GetMapping("/{id}")
    public TenantResponse getTenant(@PathVariable UUID id) {
        return TenantResponse.from(tenantService.getTenant(id));
    }
}
