# Zero-downtime schema evolution: Expand–Contract

A naive schema change (rename a column, make a column `NOT NULL`, change a type)
breaks a running system the instant it lands: old application instances are still
running against the old shape while the new shape is being applied. **Expand–
Contract** (a.k.a. parallel change) removes that window by splitting one breaking
change into a sequence of individually-safe, backward-compatible steps.

The rule: **at no single moment may the running application and the database be
incompatible.**

## The three phases

Take a worked example: replacing `NICKNAME` with `DISPLAY_NAME` on
`ACCOUNT.ACCOUNT` without downtime.

### 1. EXPAND — add the new shape, keep the old

A purely additive, backward-compatible migration. The new column is nullable so
existing rows and old application instances are unaffected.

```sql
-- TAG: PROJ-200
-- SCHEMA: ACCOUNT
-- TYPE: DDL
-- TABLES: ACCOUNT
-- DESCRIPTION: [EXPAND] add DISPLAY_NAME alongside NICKNAME
ALTER TABLE ACCOUNT.ACCOUNT ADD (DISPLAY_NAME VARCHAR2(128));
COMMENT ON COLUMN ACCOUNT.ACCOUNT.DISPLAY_NAME IS 'Public display name (supersedes NICKNAME)';
```

The application is deployed to **dual-write**: it writes both `NICKNAME` and
`DISPLAY_NAME`, and reads `DISPLAY_NAME` falling back to `NICKNAME`. Old and new
application instances both keep working.

### 2. MIGRATE / BACKFILL — copy existing data

A separate, idempotent, restartable DML that backfills history. For a large
table this runs in batches via a backfill/sync job rather than one statement, so
it never holds a long lock.

```sql
-- TAG: PROJ-201
-- SCHEMA: ACCOUNT
-- TYPE: DML
-- TABLES: ACCOUNT
-- DESCRIPTION: [MIGRATE] backfill DISPLAY_NAME from NICKNAME where empty
UPDATE ACCOUNT.ACCOUNT
   SET DISPLAY_NAME = NICKNAME
 WHERE DISPLAY_NAME IS NULL;
COMMIT;
```

Once every row is backfilled **and** every running instance is dual-writing, the
new column is fully populated and authoritative.

### 3. CONTRACT — remove the old shape

Only after the new column is proven authoritative across all instances:

```sql
-- TAG: PROJ-202
-- SCHEMA: ACCOUNT
-- TYPE: DDL
-- TABLES: ACCOUNT
-- DESCRIPTION: [CONTRACT] enforce DISPLAY_NAME NOT NULL, drop NICKNAME
ALTER TABLE ACCOUNT.ACCOUNT MODIFY (DISPLAY_NAME VARCHAR2(128) NOT NULL);
ALTER TABLE ACCOUNT.ACCOUNT DROP COLUMN NICKNAME;
```

The application is deployed once more to drop the `NICKNAME` write/read paths.

## Sequencing across deploys

| Step | Schema | Application | Safe because |
|------|--------|-------------|--------------|
| 0 | old | reads/writes `NICKNAME` | baseline |
| 1 | EXPAND (DISPLAY_NAME nullable) | unchanged | additive only; old code ignores new column |
| 2 | — | dual-write, read new→old fallback | both columns valid |
| 3 | MIGRATE (backfill, batched) | dual-write | new column becomes complete |
| 4 | — | read/write `DISPLAY_NAME` only | new column authoritative |
| 5 | CONTRACT (NOT NULL, drop old) | unchanged | no instance references `NICKNAME` anymore |

Each row changes **either** schema **or** application, never both at once, and
each individual step is backward-compatible with the deployment before it.

## How this maps onto the pipeline

- Each phase is an ordinary pending SQL with its own TAG, so EXPAND / MIGRATE /
  CONTRACT are three reviewed, tested, archived changes — the audit trail records
  the whole evolution.
- The phases are gated on **application rollout**, not just on time. CONTRACT
  must not merge until the read/write-old paths are gone from every running
  instance.
- The backfill is owned by a dedicated, idempotent sync job so it can be paused,
  resumed, and re-run without double-applying.

## Type changes and document stores

- **Type change** (e.g. `NUMBER` → `VARCHAR2`): same shape — add a new column of
  the new type (EXPAND), dual-write + backfill (MIGRATE), switch reads and drop
  the old column (CONTRACT).
- **MongoDB**: the document store has no enforced column list, but a
  `$jsonSchema` validator does. Expand by relaxing the validator / adding the
  new field as optional, dual-write + backfill per collection, then contract by
  tightening `required` and removing the old field.
