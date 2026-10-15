-- Example customer account setup. Replace hash placeholder as above.
INSERT INTO CUSTOMER (CUSTOMER_ID, CUSTOMER_NAME, EMAIL, PHONE, ADDRESS)
VALUES (CUSTOMER_SEQ.NEXTVAL, 'Demo Client', 'client@example.com', '9000000000', 'Demo address');

-- Capture the generated CUSTOMER_ID using:
-- SELECT CUSTOMER_ID FROM CUSTOMER WHERE EMAIL='client@example.com';
-- Then replace 1000 below with that actual ID.
INSERT INTO USER_ACCOUNT (USER_ID, USERNAME, PASSWORD_HASH, ROLE, CUSTOMER_ID, ACTIVE)
VALUES (USER_ACCOUNT_SEQ.NEXTVAL, 'client', '<PASTE_PBKDF2_HASH_HERE>', 'CLIENT', 1000, 'Y');
COMMIT;
