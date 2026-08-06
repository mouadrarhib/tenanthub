package com.tenanthub.billing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

// A trusted local copy of the tenant's plan limits, kept in sync via the
// tenant.created event - Billing Service never calls Tenant Service directly for
// this (same "copied and trusted" tenant_id pattern the db-schema doc calls out).
@Entity
@Table(name = "tenant_plan_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TenantPlanLimits {

    @Id
    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "max_users", nullable = false)
    private int maxUsers;

    @Column(name = "max_projects", nullable = false)
    private int maxProjects;
}
