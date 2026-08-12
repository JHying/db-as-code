-- =============================================================================
-- account/oracle-ddl.sql  —  ACCOUNT schema slice (current full schema)
-- Squash mode. This per-schema view is what a single service pulls (via the
-- Maven-published artifact) to build its TestContainers instance.
-- Snapshot state: BEFORE pending PROJ-101 (add STATUS) is synced.
-- =============================================================================

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
