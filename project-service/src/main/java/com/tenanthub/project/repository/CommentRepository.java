package com.tenanthub.project.repository;

import com.tenanthub.project.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTaskId(UUID taskId);

    Optional<Comment> findByIdAndTask_Project_TenantId(UUID id, UUID tenantId);
}
