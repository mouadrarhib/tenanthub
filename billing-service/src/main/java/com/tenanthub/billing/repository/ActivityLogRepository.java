package com.tenanthub.billing.repository;

import com.tenanthub.billing.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    List<ActivityLog> findByTenantIdOrderByReceivedAtDesc(UUID tenantId);
}
