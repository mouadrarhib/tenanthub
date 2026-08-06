package com.tenanthub.tenant.repository;

import com.tenanthub.tenant.entity.Plan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository/integration tests against the real local Postgres (schema managed by
 * Hibernate ddl-auto=update), not an embedded database - same reasoning as the other
 * services' repository tests. Each test runs in a transaction that's rolled back
 * afterward, so nothing persists between runs. Plan names use a random suffix to avoid
 * colliding with the unique constraint against plans already seeded for real use
 * (e.g. "free").
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlanRepository planRepository;

    @Test
    void save_persistsGeneratedIdAndFields() {
        Plan plan = Plan.builder()
                .name("TEST_PLAN_" + UUID.randomUUID())
                .maxUsers(5)
                .maxProjects(3)
                .priceCents(0)
                .build();

        Plan saved = planRepository.save(plan);
        entityManager.flush();
        entityManager.clear();

        Optional<Plan> found = planRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(plan.getName());
        assertThat(found.get().getMaxUsers()).isEqualTo(5);
        assertThat(found.get().getMaxProjects()).isEqualTo(3);
        assertThat(found.get().getPriceCents()).isEqualTo(0);
    }

    @Test
    void findById_notFound_returnsEmpty() {
        Optional<Plan> found = planRepository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }
}
