
ALTER TABLE nev_rewards
    ADD COLUMN category VARCHAR(40) NULL AFTER image_b64;


CREATE TABLE IF NOT EXISTS nev_reward_categories (
    code      VARCHAR(40)  PRIMARY KEY,
    label_en  VARCHAR(80)  NOT NULL,
    label_ro  VARCHAR(80)  NOT NULL,
    sort_order INT         NOT NULL DEFAULT 0
);

INSERT INTO nev_reward_categories (code, label_en, label_ro, sort_order) VALUES
    ('BOOKS',   'Books',   'Cărți',     1),
    ('CAFE',    'Café',    'Cafenea',   2),
    ('MUSIC',   'Music',   'Muzică',    3),
    ('PRINT',   'Print',   'Print',     4),
    ('GOODS',   'Goods',   'Produse',   5),
    ('PARTNER', 'Partner', 'Partener',  6);


UPDATE nev_rewards SET category = 'CAFE'   WHERE id = 'cfdd50ad-928b-47a8-8bee-2ecb4f89baba';
UPDATE nev_rewards SET category = 'BOOKS'  WHERE id = 'a1b2c3d4-e5f6-7890-abcd-ef1234567890';
UPDATE nev_rewards SET category = 'MUSIC'  WHERE id = 'b2c3d4e5-f6a7-8901-bcde-f12345678901';
UPDATE nev_rewards SET category = 'PRINT'  WHERE id = 'c3d4e5f6-a7b8-9012-cdef-123456789012';


UPDATE nev_rewards SET category = 'PARTNER' WHERE category IS NULL;
