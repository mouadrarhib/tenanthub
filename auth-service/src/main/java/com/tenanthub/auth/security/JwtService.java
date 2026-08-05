package com.tenanthub.auth.security;

import com.tenanthub.auth.entity.Role;
import com.tenanthub.auth.entity.User;
import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtService {

    private final SecretKeySpec key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
        // Pinned to HS256 explicitly (not Keys.hmacShaKeyFor + signWith(key), which
        // auto-upgrades to HS384/HS512 once the secret is long enough) - project-service's
        // JwtDecoder hardcodes HS256, so a silent algorithm upgrade here would break it.
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles().stream().map(Role::getName).toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
