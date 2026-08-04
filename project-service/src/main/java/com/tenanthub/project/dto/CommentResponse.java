package com.tenanthub.project.dto;

import com.tenanthub.project.entity.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID taskId,
        UUID authorUserId,
        String content,
        LocalDateTime createdAt
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getAuthorUserId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
