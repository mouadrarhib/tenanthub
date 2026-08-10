package com.tenanthub.billing.controller;

import com.tenanthub.billing.dto.UsageResponse;
import com.tenanthub.billing.entity.TenantPlanLimits;
import com.tenanthub.billing.repository.TenantPlanLimitsRepository;
import com.tenanthub.billing.repository.UsageRecordRepository;
import com.tenanthub.billing.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class UsageController {

    private static final String PROJECTS_COUNT_METRIC = "projects_count";

    private final TenantPlanLimitsRepository tenantPlanLimitsRepository;
    private final UsageRecordRepository usageRecordRepository;

    @GetMapping("/usage")
    public UsageResponse getUsage(@AuthenticationPrincipal Jwt jwt) {
        var tenantId = TenantContext.tenantId(jwt);
        TenantPlanLimits limits = tenantPlanLimitsRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No plan limits recorded for this tenant yet"));

        long projectsUsed = usageRecordRepository.countByTenantIdAndMetricName(tenantId, PROJECTS_COUNT_METRIC);
        return UsageResponse.of(limits, projectsUsed);
    }
}
