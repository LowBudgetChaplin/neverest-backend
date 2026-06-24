
ALTER TABLE nev_users
    ADD COLUMN phone_number VARCHAR(30)  NULL AFTER display_name,
    ADD COLUMN avatar_b64   MEDIUMTEXT   NULL AFTER phone_number;
