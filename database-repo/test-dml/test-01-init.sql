-- Seed data for integration tests (loaded after the schema).
-- Character data is single-quoted; numeric data is not; Oracle DML ends with COMMIT.
INSERT INTO ACCOUNT (ID, USERNAME, NICKNAME, BALANCE, CREATED_AT)
VALUES (1, 'alice', 'Alice', 100.0000, SYSTIMESTAMP);
INSERT INTO ACCOUNT (ID, USERNAME, NICKNAME, BALANCE, CREATED_AT)
VALUES (2, 'bob', 'Bob', 0.0000, SYSTIMESTAMP);
COMMIT;
