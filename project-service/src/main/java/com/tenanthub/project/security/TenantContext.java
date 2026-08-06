package com.tenanthub.project.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

// Every JWT auth-service issues carries a tenantId claim (see JwtService there) -
// this is the single place that reads it back out, so every controller scopes
// queries to the caller's own tenant the same way.
public final class TenantContext {

    private TenantContext() {
    }

    public static UUID tenantId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("tenantId"));
    }
}
