package com.tenanthub.project.repository;

import com.tenanthub.project.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTaskId(UUID taskId);
}
