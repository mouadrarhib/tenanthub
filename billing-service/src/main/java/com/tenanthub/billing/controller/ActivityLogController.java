package com.tenanthub.billing.controller;

import com.tenanthub.billing.dto.ActivityLogResponse;
import com.tenanthub.billing.repository.ActivityLogRepository;
import com.tenanthub.billing.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/billing/activity-log")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogRepository activityLogRepository;

    @GetMapping
    public List<ActivityLogResponse> getActivityLog(@AuthenticationPrincipal Jwt jwt) {
        var tenantId = TenantContext.tenantId(jwt);
        return activityLogRepository.findByTenantIdOrderByReceivedAtDesc(tenantId).stream()
                .map(ActivityLogResponse::of)
                .toList();
    }
}
