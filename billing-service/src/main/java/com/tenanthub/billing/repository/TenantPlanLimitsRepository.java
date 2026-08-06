package com.tenanthub.billing.repository;

import com.tenanthub.billing.entity.TenantPlanLimits;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantPlanLimitsRepository extends JpaRepository<TenantPlanLimits, UUID> {
}
