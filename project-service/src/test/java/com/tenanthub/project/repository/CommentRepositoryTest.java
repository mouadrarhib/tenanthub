package com.tenanthub.project.repository;

import com.tenanthub.project.entity.Comment;
import com.tenanthub.project.entity.Project;
import com.tenanthub.project.entity.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private Task persistTask() {
        Project project = Project.builder()
                .tenantId(UUID.randomUUID())
                .name("Launch Website")
                .build();
        entityManager.persist(project);

        Task task = Task.builder()
                .project(project)
                .title("Design homepage")
                .build();
        entityManager.persist(task);
        return task;
    }

    @Test
    void save_persistsFieldsAndCreatedAt() {
        Task task = persistTask();
        UUID authorId = UUID.randomUUID();

        Comment comment = Comment.builder()
                .task(task)
                .authorUserId(authorId)
                .content("Looks good")
                .build();

        Comment saved = commentRepository.save(comment);
        entityManager.flush();
        entityManager.clear();

        Optional<Comment> found = commentRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("Looks good");
        assertThat(found.get().getAuthorUserId()).isEqualTo(authorId);
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getTask().getId()).isEqualTo(task.getId());
    }

    @Test
    void findByTaskId_returnsOnlyCommentsForThatTask() {
        Task taskA = persistTask();
        Task taskB = persistTask();

        entityManager.persist(Comment.builder().task(taskA).authorUserId(UUID.randomUUID()).content("A1").build());
        entityManager.persist(Comment.builder().task(taskA).authorUserId(UUID.randomUUID()).content("A2").build());
        entityManager.persist(Comment.builder().task(taskB).authorUserId(UUID.randomUUID()).content("B1").build());
        entityManager.flush();

        List<Comment> commentsForA = commentRepository.findByTaskId(taskA.getId());

        assertThat(commentsForA).extracting(Comment::getContent).containsExactlyInAnyOrder("A1", "A2");
    }
}
