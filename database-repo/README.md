# database-repo (central source of truth)

The single source of truth for the platform's database schema. It is updated
**only** by the Stage-3 sync when an application repo merges a pending change —
never by hand.

## Layout

| Path | Purpose |
|------|---------|
| `oracle-ddl.sql` | Current full relational schema (all schemas), squash mode |
| `mongo-ddl.js` | Current full document collections, with `$jsonSchema` validators |
| `changelog.md` | Whole-DB migration history (`GIT_COMMIT / TAG / DATE / RESULT / DESCRIPTION`) |
| `account/` | Per-schema slice — what a single service pulls to build its container |
| `migrations/` | Archived original pending SQL, kept for audit |
| `test-dml/` | Seed DML for integration tests |
| `docker-compose.test.yml` | Full-stack E2E environment (Stage 5) |

## Publishing

On each environment branch the schema is published to a Maven registry,
versioned by branch/environment, so services and IaC can pull an exact schema
version:

```
develop  → schema:<version>-develop
staging  → schema:<version>-staging
production → schema:<version>
```

## Environment promotion

Environment branches merge on a release cadence (`develop → staging →
production`); each merge applies the delta to that environment's **live**
database while **retaining existing data**, governed by Flyway / Liquibase /
Bytebase. Because every environment deploys from this one source, there is no
cross-environment drift.
