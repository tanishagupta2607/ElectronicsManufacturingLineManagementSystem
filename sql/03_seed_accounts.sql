-- Run AFTER the Java project is compiled.
-- Generate hashes with the app's PasswordUtil; never store plain-text passwords.
-- Easiest initial setup:
-- 1) Temporarily compile/run the helper in README instructions to generate a PBKDF2 hash.
-- 2) Replace <PASTE_PBKDF2_HASH_HERE> with that generated hash.
-- 3) Run these inserts as EMLS_APP.
--
-- An admin account is required to create staff accounts in the app.
-- Change admin username/password during setup.
INSERT INTO USER_ACCOUNT (USER_ID, USERNAME, PASSWORD_HASH, ROLE, CUSTOMER_ID, ACTIVE)
VALUES (USER_ACCOUNT_SEQ.NEXTVAL, 'admin', '<PASTE_PBKDF2_HASH_HERE>', 'ADMIN', NULL, 'Y');
COMMIT;
