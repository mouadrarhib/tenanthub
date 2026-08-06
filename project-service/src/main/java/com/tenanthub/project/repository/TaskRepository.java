package com.tenanthub.project.repository;

import com.tenanthub.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByProjectId(UUID projectId);

    Optional<Task> findByIdAndProject_TenantId(UUID id, UUID tenantId);
}
