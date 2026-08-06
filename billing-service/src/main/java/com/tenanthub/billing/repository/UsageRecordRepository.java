package com.tenanthub.billing.repository;

import com.tenanthub.billing.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {

    long countByTenantIdAndMetricName(UUID tenantId, String metricName);
}
