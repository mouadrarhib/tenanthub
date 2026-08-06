package com.tenanthub.billing.repository;

import com.tenanthub.billing.entity.UsageRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsageRecordRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsageRecordRepository usageRecordRepository;

    @Test
    void save_persistsGeneratedIdAndRecordedAt() {
        UUID tenantId = UUID.randomUUID();
        UsageRecord record = UsageRecord.builder().tenantId(tenantId).metricName("projects_count").value(1).build();

        UsageRecord saved = usageRecordRepository.save(record);
        entityManager.flush();
        entityManager.clear();

        UsageRecord found = entityManager.find(UsageRecord.class, saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getMetricName()).isEqualTo("projects_count");
        assertThat(found.getRecordedAt()).isNotNull();
    }

    @Test
    void countByTenantIdAndMetricName_countsOnlyMatchingRows() {
        UUID tenantId = UUID.randomUUID();
        UUID otherTenantId = UUID.randomUUID();
        entityManager.persist(UsageRecord.builder().tenantId(tenantId).metricName("projects_count").value(1).build());
        entityManager.persist(UsageRecord.builder().tenantId(tenantId).metricName("projects_count").value(1).build());
        entityManager.persist(UsageRecord.builder().tenantId(tenantId).metricName("users_count").value(1).build());
        entityManager.persist(UsageRecord.builder().tenantId(otherTenantId).metricName("projects_count").value(1).build());
        entityManager.flush();

        long count = usageRecordRepository.countByTenantIdAndMetricName(tenantId, "projects_count");

        assertThat(count).isEqualTo(2);
    }
}
