package com.tenanthub.billing.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public final class TenantContext {

    private TenantContext() {
    }

    public static UUID tenantId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("tenantId"));
    }
}
