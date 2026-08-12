-- =============================================================================
-- oracle-ddl.sql  —  CURRENT FULL SCHEMA (squash mode, all schemas)
-- Regenerated from `current + pending` on every Stage-3 sync. Do not hand-edit;
-- change it by adding pending SQL in an application repo's docs/db/.
--
-- Convention note: object names are UPPERCASE; every table/column carries a
-- comment; statements end with ';'; no DROP in the source of truth. In the
-- production source of truth each object is additionally prefixed with its
-- schema owner (e.g. ACCOUNT.ACCOUNT); names are left unqualified here so the
-- reference is runnable against a single TestContainers user.
--
-- Snapshot state: pending change PROJ-101 (add STATUS) has NOT yet been synced;
-- it is in-flight in sample-service/docs/db. Stage 3 is what folds it into this
-- file and the changelog.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- CORE schema
-- -----------------------------------------------------------------------------
CREATE TABLE APP_USER
(
  ID          NUMBER(19)    NOT NULL,
  USERNAME    VARCHAR2(64)  NOT NULL,
  EMAIL       VARCHAR2(256) NOT NULL,
  CREATED_AT  TIMESTAMP(6)  DEFAULT SYSTIMESTAMP NOT NULL
);
COMMENT ON TABLE  APP_USER            IS 'Platform user account';
COMMENT ON COLUMN APP_USER.ID         IS 'Surrogate primary key';
COMMENT ON COLUMN APP_USER.USERNAME   IS 'Unique login name';
COMMENT ON COLUMN APP_USER.EMAIL      IS 'Contact email';
COMMENT ON COLUMN APP_USER.CREATED_AT IS 'Row creation timestamp';

ALTER TABLE APP_USER ADD CONSTRAINT APP_USER_PK PRIMARY KEY (ID) USING INDEX;
ALTER TABLE APP_USER ADD CONSTRAINT APP_USER_UK UNIQUE (USERNAME) USING INDEX;

CREATE SEQUENCE SEQAPP_USER
  MINVALUE 1 NOMAXVALUE INCREMENT BY 1 START WITH 1 CACHE 100 NOORDER;

-- -----------------------------------------------------------------------------
-- ACCOUNT schema
-- -----------------------------------------------------------------------------
CREATE TABLE ACCOUNT
(
  ID          NUMBER(19)     NOT NULL,
  USERNAME    VARCHAR2(64)   NOT NULL,
  NICKNAME    VARCHAR2(128),
  BALANCE     NUMBER(18,4)   DEFAULT 0 NOT NULL,
  CREATED_AT  TIMESTAMP(6)   DEFAULT SYSTIMESTAMP NOT NULL
);
COMMENT ON TABLE  ACCOUNT            IS 'Per-user account';
COMMENT ON COLUMN ACCOUNT.ID         IS 'Surrogate primary key';
COMMENT ON COLUMN ACCOUNT.USERNAME   IS 'Owning login name';
COMMENT ON COLUMN ACCOUNT.NICKNAME   IS 'Public display name';
COMMENT ON COLUMN ACCOUNT.BALANCE    IS 'Account balance';
COMMENT ON COLUMN ACCOUNT.CREATED_AT IS 'Row creation timestamp';

ALTER TABLE ACCOUNT ADD CONSTRAINT ACCOUNT_PK PRIMARY KEY (ID) USING INDEX;
CREATE INDEX ACCOUNT_USERNAME ON ACCOUNT (USERNAME ASC);

CREATE SEQUENCE SEQACCOUNT
  MINVALUE 1 NOMAXVALUE INCREMENT BY 1 START WITH 1 CACHE 100 NOORDER;
