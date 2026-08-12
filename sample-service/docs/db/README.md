# Schema history for this service

Every DDL/DML change ever made to this service's schema, one file per ticket:
`<TAG>.sql` / `<TAG>.js`, each with the mandatory header (see the project
README). This directory **is** the service's migration history — not a staging
area that gets emptied out. Tests replay every file here, in full, against a
fresh Testcontainers instance to reconstruct the schema they run against (see
`BaseOracleContainer`), so a ticket's file stays useful as a test fixture long
after it has been synced to the central schema in `database-repo/`.

| File | Status |
|------|--------|
| `PROJ-100.sql` | Already synced — the original table, merged into `database-repo/` |
| `PROJ-101.sql` | **Pending** — in flight, not yet synced (see Stage 3 in `docs/workflow.md`) |
| `PROJ-102.js`  | **Pending** — MongoDB DDL; adds an `AUDIT_LOG` collection |

New changes are validated by the CI rule checks (header
completeness, lint, conflict-with-central-schema) before merge, and folded
into the central squashed schema by the Stage-3 sync — at which point the file
here is marked synced in `changelog.md`-equivalent local tracking, but is not
deleted: removing it would silently shrink the schema this service's own
tests are constructed from.

## Header format

SQL (`.sql`):

```
-- TAG: {TICKET_ID}
-- SCHEMA: {SCHEMA_NAME}
-- TYPE: DDL | DML | BOTH
-- TABLES: {TABLE_NAME}
-- DESCRIPTION: {what changed and why}
-- BREAKING: Y | N
```

MongoDB JS (`.js`):

```
// TAG: {TICKET_ID}
// SCHEMA: {SCHEMA_NAME}
// TYPE: DDL | DML | BOTH
// COLLECTIONS: {COLLECTION_NAME}
// DESCRIPTION: {what changed and why}
// BREAKING: Y | N
```

`BREAKING` flags a change that is not safe to roll out as a plain rolling
deploy (a column type change, a non-nullable column with no default, a drop) —
see [`docs/expand-contract.md`](../../../docs/expand-contract.md) for how a
breaking change is split into safe, sequenced steps instead of shipped as one.

## DDL vs DML scope

- **DDL is mandatory** for any schema change — table creation, column
  changes, index/constraint changes.
- **DML is narrow on purpose**: only data the system cannot start correctly
  without (seed/bootstrap rows — a default category, a required config row).
  Ordinary test data does not belong here; it belongs in the test itself or in
  `database-repo/test-dml/`.

Statements are written **unqualified**; the target schema is declared in the
`SCHEMA` header and qualified by the per-environment apply step. (The central
source-of-truth DDL additionally carries the schema owner per DBA convention.)
