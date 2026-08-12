package com.example.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The schema/Entity contract test.
 * <p>
 * {@link BaseOracleContainer} initialises the container from every file in
 * {@code docs/db/} — {@code PROJ-100.sql} (the original table) and
 * {@code PROJ-101.sql} (the in-flight pending change that adds
 * {@code STATUS}) — and starts the persistence context with
 * {@code hibernate.ddl-auto=validate}. The test passes only if the
 * {@link Account} Entity matches the schema produced by replaying that full
 * history, in particular the {@code STATUS} column, which exists *only*
 * because {@code PROJ-101.sql} added it.
 * <p>
 * Delete {@code docs/db/PROJ-101.sql} (simulating a developer who changed
 * their local dev DB by hand but forgot to write the pending SQL) and this
 * test fails at context start-up: Hibernate {@code validate} finds an Entity
 * field with no backing column. That is the omission the workflow exists to
 * catch — before it reaches review, let alone production.
 */
@DataJpaTest
class AccountRepositoryTest extends BaseOracleContainer {

    @Autowired
    private AccountRepository repository;

    @Test
    void persists_and_reads_back_including_the_pending_status_column() {
        Account saved = repository.save(
                new Account("alice", "Alice", new BigDecimal("100.0000"), Boolean.TRUE));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<Account> found = repository.findByUsername("alice");
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isTrue();   // column added by PROJ-101
        assertThat(found.get().getBalance()).isEqualByComparingTo("100.0000");
    }
}
