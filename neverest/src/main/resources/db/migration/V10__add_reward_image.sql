-- V10: Optional reward image stored as a base64 data URI (NULL = use default art).
ALTER TABLE nev_rewards
    ADD COLUMN image_b64 MEDIUMTEXT NULL AFTER address;
