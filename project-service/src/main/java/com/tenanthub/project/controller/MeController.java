package com.tenanthub.project.controller;

import com.tenanthub.project.dto.MeResponse;
import com.tenanthub.project.security.UserContext;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

    @GetMapping
    public MeResponse getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return new MeResponse(UserContext.userId(jwt));
    }
}
