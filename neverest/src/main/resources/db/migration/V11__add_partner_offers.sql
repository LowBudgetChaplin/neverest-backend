-- V10: Partner accounts advertising space (offers / promotions).
-- Partner users (role = PARTNER) own these rows; admins do not edit them and
-- partners cannot edit admin content. Ownership is enforced in the service.
CREATE TABLE IF NOT EXISTS nev_partner_offers (
    id              CHAR(36)     PRIMARY KEY,
    owner_user_id   CHAR(36)     NOT NULL,
    brand           VARCHAR(120) NOT NULL,
    title           VARCHAR(180) NOT NULL,
    description     VARCHAR(700) NULL,
    discount_label  VARCHAR(80)  NULL,
    image_b64       MEDIUMTEXT   NULL,
    link_url        VARCHAR(500) NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    valid_until     TIMESTAMP    NULL,
    created_at      TIMESTAMP    NOT NULL,
    CONSTRAINT fk_nev_partner_offers_owner
        FOREIGN KEY (owner_user_id) REFERENCES nev_users(id)
);

CREATE INDEX idx_nev_partner_offers_active ON nev_partner_offers(active);
CREATE INDEX idx_nev_partner_offers_owner ON nev_partner_offers(owner_user_id);
