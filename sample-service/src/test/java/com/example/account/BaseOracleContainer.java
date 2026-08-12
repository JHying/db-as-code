package com.example.account;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

/**
 * Shared Oracle Testcontainers base.
 * <p>
 * A subclass that adds {@code @DataJpaTest} gets a container automatically:
 * one Oracle instance, started once and reused across every test class that
 * extends this base, each test still isolated by {@code @DataJpaTest}'s
 * per-test transaction rollback.
 * <pre>
 * {@code @DataJpaTest}
 * class FooRepositoryTest extends BaseOracleContainer { ... }
 * </pre>
 * <p>
 * The schema-locations property is the load-bearing line: it points at the
 * <b>same {@code docs/db/} directory</b> that holds the real, DBA-reviewed
 * pending SQL — not a separate copy maintained only for tests. The build
 * exposes that directory on the test classpath (see the project POM's
 * {@code testResources}), so {@code docs/db/*.sql} here resolves to the
 * project's actual migration history: every ticket-numbered file accumulated
 * for this service to date, replayed in full to reconstruct the schema this
 * test runs against. There is exactly one copy of this SQL in the repository.
 */
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        // Validate, never auto-generate: a mismatch between the Entity and the
        // applied DDL fails the context, not a silent schema patch.
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:docs/db/*.sql"
})
public abstract class BaseOracleContainer {

    @Container
    @ServiceConnection
    protected static final OracleContainer ORACLE =
            new OracleContainer("gvenzl/oracle-free:slim-faststart")
                    .withReuse(true);
}
