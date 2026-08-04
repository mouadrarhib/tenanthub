package com.tenanthub.project.repository;

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

/**
 * Repository/integration tests against the real local Postgres (schema managed by
 * Flyway), not an embedded database - the schema uses Postgres-specific features
 * (UUID columns, gen_random_uuid(), CHECK constraints) that an in-memory substitute
 * wouldn't exercise faithfully. Each test runs in a transaction that's rolled back
 * afterward, so nothing persists between runs.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjectRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void save_persistsGeneratedIdAndCreatedAt() {
        Project project = Project.builder()
                .tenantId(UUID.randomUUID())
                .name("Launch Website")
                .description("Q3 relaunch")
                .build();

        Project saved = projectRepository.save(project);
        entityManager.flush();
        entityManager.clear();

        Optional<Project> found = projectRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Launch Website");
        assertThat(found.get().getDescription()).isEqualTo("Q3 relaunch");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void delete_cascadesToTasks() {
        Project project = Project.builder()
                .tenantId(UUID.randomUUID())
                .name("Launch Website")
                .build();
        entityManager.persist(project);

        Task task = Task.builder()
                .project(project)
                .title("Design homepage")
                .build();
        // Hibernate cascades CascadeType.ALL by walking the in-memory collection, not
        // the FK - the parent side must know about the child or it won't cascade.
        project.getTasks().add(task);
        entityManager.persist(task);
        entityManager.flush();
        UUID taskId = task.getId();

        projectRepository.delete(project);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Task.class, taskId)).isNull();
    }

    @Test
    void findAll_returnsAllProjects() {
        entityManager.persist(Project.builder().tenantId(UUID.randomUUID()).name("A").build());
        entityManager.persist(Project.builder().tenantId(UUID.randomUUID()).name("B").build());
        entityManager.flush();

        List<Project> all = projectRepository.findAll();

        assertThat(all).extracting(Project::getName).contains("A", "B");
    }
}
