package com.tenanthub.project.repository;

import com.tenanthub.project.entity.Comment;
import com.tenanthub.project.entity.Project;
import com.tenanthub.project.entity.Task;
import com.tenanthub.project.entity.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private Project persistProject() {
        Project project = Project.builder()
                .tenantId(UUID.randomUUID())
                .name("Launch Website")
                .build();
        entityManager.persist(project);
        return project;
    }

    @Test
    void save_persistsDefaultStatusAndFields() {
        Project project = persistProject();
        UUID assignee = UUID.randomUUID();

        Task task = Task.builder()
                .project(project)
                .title("Design homepage")
                .assigneeUserId(assignee)
                .dueDate(LocalDate.of(2026, 9, 1))
                .build();

        Task saved = taskRepository.save(task);
        entityManager.flush();
        entityManager.clear();

        Optional<Task> found = taskRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(found.get().getAssigneeUserId()).isEqualTo(assignee);
        assertThat(found.get().getProject().getId()).isEqualTo(project.getId());
    }

    @Test
    void findByProjectId_returnsOnlyTasksForThatProject() {
        Project projectA = persistProject();
        Project projectB = persistProject();

        entityManager.persist(Task.builder().project(projectA).title("A1").build());
        entityManager.persist(Task.builder().project(projectA).title("A2").build());
        entityManager.persist(Task.builder().project(projectB).title("B1").build());
        entityManager.flush();

        List<Task> tasksForA = taskRepository.findByProjectId(projectA.getId());

        assertThat(tasksForA).extracting(Task::getTitle).containsExactlyInAnyOrder("A1", "A2");
    }

    @Test
    void delete_cascadesToComments() {
        Project project = persistProject();
        Task task = Task.builder().project(project).title("Design homepage").build();
        entityManager.persist(task);

        Comment comment = Comment.builder()
                .task(task)
                .authorUserId(UUID.randomUUID())
                .content("Looks good")
                .build();
        // Hibernate cascades CascadeType.ALL by walking the in-memory collection, not
        // the FK - the parent side must know about the child or it won't cascade.
        task.getComments().add(comment);
        entityManager.persist(comment);
        entityManager.flush();
        UUID commentId = comment.getId();

        taskRepository.delete(task);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Comment.class, commentId)).isNull();
    }
}
