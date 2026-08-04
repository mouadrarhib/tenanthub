package com.tenanthub.project.controller;

import com.tenanthub.project.dto.CommentRequest;
import com.tenanthub.project.dto.CommentResponse;
import com.tenanthub.project.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(@PathVariable UUID taskId, @Valid @RequestBody CommentRequest request) {
        return CommentResponse.from(
                commentService.createComment(taskId, request.authorUserId(), request.content())
        );
    }

    @GetMapping("/api/tasks/{taskId}/comments")
    public List<CommentResponse> listComments(@PathVariable UUID taskId) {
        return commentService.listCommentsByTask(taskId).stream().map(CommentResponse::from).toList();
    }

    @GetMapping("/api/comments/{id}")
    public CommentResponse getComment(@PathVariable UUID id) {
        return CommentResponse.from(commentService.getComment(id));
    }

    @DeleteMapping("/api/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID id) {
        commentService.deleteComment(id);
    }
}
