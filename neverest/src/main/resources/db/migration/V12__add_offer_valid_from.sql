
ALTER TABLE nev_partner_offers
    ADD COLUMN valid_from TIMESTAMP NULL AFTER active;
