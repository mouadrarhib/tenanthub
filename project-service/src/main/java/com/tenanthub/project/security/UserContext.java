package com.tenanthub.project.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

// Every JWT auth-service issues carries the caller's user id as the standard "sub"
// claim (see JwtService there) - this is the single place that reads it back out,
// same pattern as TenantContext for the tenantId claim.
public final class UserContext {

    private UserContext() {
    }

    public static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
