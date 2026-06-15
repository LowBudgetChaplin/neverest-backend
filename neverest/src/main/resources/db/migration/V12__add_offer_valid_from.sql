-- V12: Schedule partner offers — when they become visible (valid_from) and
-- when they expire (valid_until already exists). Public listing filters by now.
ALTER TABLE nev_partner_offers
    ADD COLUMN valid_from TIMESTAMP NULL AFTER active;
