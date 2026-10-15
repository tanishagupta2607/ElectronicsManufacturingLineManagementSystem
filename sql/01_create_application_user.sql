-- Run as an Oracle DBA in the correct PDB/service.
-- Change the password before using this outside a local class demo.
CREATE USER EMLS_APP IDENTIFIED BY "ChangeThis_AppPassword_26";
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE VIEW TO EMLS_APP;
ALTER USER EMLS_APP QUOTA UNLIMITED ON USERS;

-- Existing manufacturing tables should be owned by EMLS_APP for this simple
-- JDBC setup, or grants/synonyms must be configured if owned by another schema.
