package com.tenanthub.auth.repository;

import com.tenanthub.auth.entity.Role;
import com.tenanthub.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository/integration tests against the real local Postgres (schema managed by
 * Hibernate ddl-auto=update), not an embedded database - same reasoning as
 * project-service's repository tests. Each test runs in a transaction that's rolled
 * back afterward, so nothing persists between runs. Role names use a random suffix to
 * avoid colliding with the unique constraint against roles already seeded by real
 * registrations (e.g. "MEMBER").
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_persistsGeneratedIdAndCreatedAt() {
        User user = User.builder()
                .email("jane-" + UUID.randomUUID() + "@tenanthub.com")
                .passwordHash("hashed")
                .build();

        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void save_persistsRolesViaJoinTable() {
        Role role = entityManager.persist(Role.builder().name("TEST_ROLE_" + UUID.randomUUID()).build());
        User user = User.builder()
                .email("withrole-" + UUID.randomUUID() + "@tenanthub.com")
                .passwordHash("hashed")
                .roles(Set.of(role))
                .build();
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findById(user.getId()).orElseThrow();
        assertThat(found.getRoles()).extracting(Role::getName).containsExactly(role.getName());
    }

    @Test
    void findByEmail_found_returnsUser() {
        String email = "found-" + UUID.randomUUID() + "@tenanthub.com";
        entityManager.persist(User.builder().email(email).passwordHash("hashed").build());
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail(email);

        assertThat(found).isPresent();
    }

    @Test
    void findByEmail_notFound_returnsEmpty() {
        Optional<User> found = userRepository.findByEmail("nobody-" + UUID.randomUUID() + "@tenanthub.com");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_true() {
        String email = "exists-" + UUID.randomUUID() + "@tenanthub.com";
        entityManager.persist(User.builder().email(email).passwordHash("hashed").build());
        entityManager.flush();

        assertThat(userRepository.existsByEmail(email)).isTrue();
    }

    @Test
    void existsByEmail_false() {
        assertThat(userRepository.existsByEmail("missing-" + UUID.randomUUID() + "@tenanthub.com")).isFalse();
    }
}
