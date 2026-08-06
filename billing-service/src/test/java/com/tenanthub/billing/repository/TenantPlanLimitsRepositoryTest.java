package com.tenanthub.billing.repository;

import com.tenanthub.billing.entity.TenantPlanLimits;
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
class TenantPlanLimitsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TenantPlanLimitsRepository tenantPlanLimitsRepository;

    @Test
    void save_persistsByTenantId() {
        UUID tenantId = UUID.randomUUID();
        TenantPlanLimits limits = TenantPlanLimits.builder()
                .tenantId(tenantId).planName("pro").maxUsers(10).maxProjects(20).build();

        tenantPlanLimitsRepository.save(limits);
        entityManager.flush();
        entityManager.clear();

        Optional<TenantPlanLimits> found = tenantPlanLimitsRepository.findById(tenantId);
        assertThat(found).isPresent();
        assertThat(found.get().getPlanName()).isEqualTo("pro");
        assertThat(found.get().getMaxProjects()).isEqualTo(20);
    }

    @Test
    void save_reDelivery_upsertsInsteadOfDuplicating() {
        UUID tenantId = UUID.randomUUID();
        tenantPlanLimitsRepository.save(
                TenantPlanLimits.builder().tenantId(tenantId).planName("free").maxUsers(1).maxProjects(1).build());
        entityManager.flush();

        tenantPlanLimitsRepository.save(
                TenantPlanLimits.builder().tenantId(tenantId).planName("pro").maxUsers(10).maxProjects(20).build());
        entityManager.flush();
        entityManager.clear();

        Optional<TenantPlanLimits> found = tenantPlanLimitsRepository.findById(tenantId);
        assertThat(found).isPresent();
        assertThat(found.get().getPlanName()).isEqualTo("pro");
        assertThat(tenantPlanLimitsRepository.count()).isEqualTo(1);
    }
}
