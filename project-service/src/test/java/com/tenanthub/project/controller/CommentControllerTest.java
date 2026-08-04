package com.tenanthub.project.controller;

import com.tenanthub.project.entity.Comment;
import com.tenanthub.project.entity.Task;
import com.tenanthub.project.exception.ResourceNotFoundException;
import com.tenanthub.project.service.CommentService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    private Comment commentFor(UUID taskId, UUID commentId, UUID authorId, String content) {
        return Comment.builder()
                .id(commentId)
                .task(Task.builder().id(taskId).build())
                .authorUserId(authorId)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createComment_returnsCreated() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Comment comment = commentFor(taskId, UUID.randomUUID(), authorId, "Looks good");

        when(commentService.createComment(taskId, authorId, "Looks good")).thenReturn(comment);

        mockMvc.perform(post("/api/tasks/{taskId}/comments", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorUserId":"%s","content":"Looks good"}
                                """.formatted(authorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Looks good"))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));

        verify(commentService).createComment(taskId, authorId, "Looks good");
    }

    @Test
    void createComment_blankContent_returnsBadRequest() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        mockMvc.perform(post("/api/tasks/{taskId}/comments", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorUserId":"%s","content":""}
                                """.formatted(authorId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_missingAuthor_returnsBadRequest() throws Exception {
        UUID taskId = UUID.randomUUID();

        mockMvc.perform(post("/api/tasks/{taskId}/comments", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"Looks good"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_taskNotFound_currentlyPropagatesUnhandled() {
        // No @ControllerAdvice yet - ResourceNotFoundException isn't mapped to a response
        // status, so MockMvc rethrows it wrapped in a ServletException.
        UUID taskId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        when(commentService.createComment(eq(taskId), eq(authorId), any()))
                .thenThrow(new ResourceNotFoundException("Task not found: " + taskId));

        ServletException exception = assertThrows(ServletException.class, () -> mockMvc.perform(
                post("/api/tasks/{taskId}/comments", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorUserId":"%s","content":"Ghost comment"}
                                """.formatted(authorId))));

        assertThat(exception.getCause()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listComments_returnsOk() throws Exception {
        UUID taskId = UUID.randomUUID();
        Comment comment = commentFor(taskId, UUID.randomUUID(), UUID.randomUUID(), "Looks good");
        when(commentService.listCommentsByTask(taskId)).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/tasks/{taskId}/comments", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Looks good"));
    }

    @Test
    void getComment_found_returnsOk() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        Comment comment = commentFor(taskId, commentId, UUID.randomUUID(), "Looks good");
        when(commentService.getComment(commentId)).thenReturn(comment);

        mockMvc.perform(get("/api/comments/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId.toString()));
    }

    @Test
    void getComment_notFound_currentlyPropagatesUnhandled() {
        UUID commentId = UUID.randomUUID();
        when(commentService.getComment(commentId)).thenThrow(new ResourceNotFoundException("Comment not found: " + commentId));

        ServletException exception = assertThrows(ServletException.class,
                () -> mockMvc.perform(get("/api/comments/{id}", commentId)));

        assertThat(exception.getCause()).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteComment_returnsNoContent() throws Exception {
        UUID commentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/comments/{id}", commentId))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(commentId);
    }
}
