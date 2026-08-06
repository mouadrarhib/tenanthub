package com.tenanthub.tenant.repository;

import com.tenanthub.tenant.entity.Plan;
import com.tenanthub.tenant.entity.Tenant;
import com.tenanthub.tenant.entity.TenantStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TenantRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TenantRepository tenantRepository;

    private Plan persistPlan() {
        Plan plan = Plan.builder()
                .name("TEST_PLAN_" + UUID.randomUUID())
                .maxUsers(5)
                .maxProjects(3)
                .priceCents(0)
                .build();
        entityManager.persist(plan);
        return plan;
    }

    @Test
    void save_persistsDefaultStatusAndCreatedAt() {
        Plan plan = persistPlan();

        Tenant tenant = Tenant.builder()
                .name("Acme Corp")
                .plan(plan)
                .build();

        Tenant saved = tenantRepository.save(tenant);
        entityManager.flush();
        entityManager.clear();

        Optional<Tenant> found = tenantRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Acme Corp");
        assertThat(found.get().getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getPlan().getId()).isEqualTo(plan.getId());
    }

    @Test
    void findById_notFound_returnsEmpty() {
        Optional<Tenant> found = tenantRepository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }
}
