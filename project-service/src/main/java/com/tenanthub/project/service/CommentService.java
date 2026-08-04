package com.tenanthub.project.service;

import com.tenanthub.project.entity.Comment;
import com.tenanthub.project.entity.Task;
import com.tenanthub.project.exception.ResourceNotFoundException;
import com.tenanthub.project.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskService taskService;

    @Transactional
    public Comment createComment(UUID taskId, UUID authorUserId, String content) {
        Task task = taskService.getTask(taskId);
        Comment comment = Comment.builder()
                .task(task)
                .authorUserId(authorUserId)
                .content(content)
                .build();
        return commentRepository.save(comment);
    }

    public Comment getComment(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + id));
    }

    public List<Comment> listCommentsByTask(UUID taskId) {
        return commentRepository.findByTaskId(taskId);
    }

    @Transactional
    public void deleteComment(UUID id) {
        commentRepository.delete(getComment(id));
    }
}
