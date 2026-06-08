-- V7: Add phone number and avatar (base64) to user profiles
ALTER TABLE nev_users
    ADD COLUMN phone_number VARCHAR(30)  NULL AFTER display_name,
    ADD COLUMN avatar_b64   MEDIUMTEXT   NULL AFTER phone_number;
