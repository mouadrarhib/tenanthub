package com.tenanthub.project.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.UUID;

// SecurityMockMvcRequestPostProcessors.jwt() relies on the security filter chain to
// move the Authentication it stages into SecurityContextHolder - these controller
// tests run with addFilters = false (see e.g. ProjectControllerTest), so that filter
// never runs. Setting SecurityContextHolder directly works instead, since MockMvc
// dispatches synchronously on the test thread and AuthenticationPrincipalArgumentResolver
// just reads the same ThreadLocal.
public final class JwtTestSupport {

    private JwtTestSupport() {
    }

    public static RequestPostProcessor withTenant(UUID tenantId) {
        return withTenant(tenantId, UUID.randomUUID());
    }

    public static RequestPostProcessor withTenant(UUID tenantId, UUID userId) {
        return request -> {
            Jwt jwt = Jwt.withTokenValue("test-token")
                    .header("alg", "none")
                    .subject(userId.toString())
                    .claim("tenantId", tenantId.toString())
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
            return request;
        };
    }
}
