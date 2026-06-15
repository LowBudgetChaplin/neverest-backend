-- V14: Expand reward categories (music, sport, coach, print, accommodation, etc.).
-- INSERT IGNORE skips codes that already exist (code is PK) — idempotent.
INSERT IGNORE INTO nev_reward_categories (code, label_en, label_ro, sort_order) VALUES
    ('MUSIC',         'Music',         'Muzică',        1),
    ('SPORT',         'Sport',         'Sport',         2),
    ('COACH',         'Coach',         'Antrenor',      3),
    ('PRINT',         'Print',         'Print',         4),
    ('ACCOMMODATION', 'Accommodation', 'Cazare',        5),
    ('CAFE',          'Café',          'Cafenea',       6),
    ('BOOKS',         'Books',         'Cărți',         7),
    ('GEAR',          'Gear',          'Echipament',    8),
    ('NUTRITION',     'Nutrition',     'Nutriție',      9),
    ('WELLNESS',      'Wellness',      'Wellness',     10),
    ('GOODS',         'Goods',         'Produse',      11),
    ('PARTNER',       'Partner',       'Partener',     12);
