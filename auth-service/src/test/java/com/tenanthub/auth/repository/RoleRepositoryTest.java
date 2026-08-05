package com.tenanthub.auth.repository;

import com.tenanthub.auth.entity.Role;
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
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void save_persistsGeneratedId() {
        Role role = Role.builder().name("TEST_ROLE_" + UUID.randomUUID()).build();

        Role saved = roleRepository.save(role);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findByName_found_returnsRole() {
        String name = "TEST_ROLE_" + UUID.randomUUID();
        entityManager.persist(Role.builder().name(name).build());
        entityManager.flush();

        Optional<Role> found = roleRepository.findByName(name);

        assertThat(found).isPresent();
    }

    @Test
    void findByName_notFound_returnsEmpty() {
        Optional<Role> found = roleRepository.findByName("TEST_ROLE_" + UUID.randomUUID());

        assertThat(found).isEmpty();
    }
}
