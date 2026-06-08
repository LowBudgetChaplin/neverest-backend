-- V8: Seed data previously hardcoded in BootstrapDataInitializer.java.
-- Now the database is the single source of truth. (MySQL syntax.)
--
-- All inserts are idempotent (INSERT ... SELECT ... FROM DUAL WHERE NOT EXISTS)
-- and FK-safe, so this migration is harmless on databases that already contain
-- the old bootstrap rows and fully sets up a fresh database.
--
-- Password for both seed accounts is "123456"
-- (BCrypt cost 10 hash, verified against BCryptPasswordEncoder).

-- ── Users ────────────────────────────────────────────────────────────────────
INSERT INTO nev_users
    (id, display_name, phone_number, avatar_b64, qr_code, auth_subject,
     total_points, available_points, points_padel, points_mountain, points_running,
     created_at, password_hash, role)
SELECT '11111111-1111-1111-1111-111111111111', 'Serban Corodescu', NULL, NULL,
       'NEV-U-SERBAN', 'serbancorodescu14@gmail.com',
       120, 120, 0, 0, 120,
       NOW(), '$2a$10$YCvAZ4jwFCMH1fjl4azjSOWGB1kp0Ja8CjZo.N2zUIFBeffNUv9Pa', 'USER'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM nev_users WHERE auth_subject = 'serbancorodescu14@gmail.com');

INSERT INTO nev_users
    (id, display_name, phone_number, avatar_b64, qr_code, auth_subject,
     total_points, available_points, points_padel, points_mountain, points_running,
     created_at, password_hash, role)
SELECT '22222222-2222-2222-2222-222222222222', 'Neverest Admin', NULL, NULL,
       'NEV-U-ADMIN', 'neverest@gmail.com',
       220, 220, 0, 0, 220,
       NOW(), '$2a$10$YCvAZ4jwFCMH1fjl4azjSOWGB1kp0Ja8CjZo.N2zUIFBeffNUv9Pa', 'ADMIN'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM nev_users WHERE auth_subject = 'neverest@gmail.com');

-- ── Strava token for Serban (only if the seed user exists) ───────────────────
INSERT INTO nev_strava_tokens
    (id, user_id, athlete_id, athlete_name, athlete_city,
     access_token, refresh_token, expires_at, scope, connected_at)
SELECT '33333333-3333-3333-3333-333333333333',
       '11111111-1111-1111-1111-111111111111',
       12345678, 'Serban Corodescu', '',
       '05f2f751bbe0cf8cf1831a42534849fe50c2d748',
       'ed71d2693da590aad6ad35d4d74fc5eed9e22747',
       1748813713, 'activity:read_all,read', NOW()
FROM DUAL
WHERE EXISTS (SELECT 1 FROM nev_users WHERE id = '11111111-1111-1111-1111-111111111111')
  AND NOT EXISTS (SELECT 1 FROM nev_strava_tokens
                  WHERE user_id = '11111111-1111-1111-1111-111111111111');

-- ── Rewards ──────────────────────────────────────────────────────────────────
INSERT INTO nev_rewards
    (id, title, partner_name, description, points_cost, stock, active, version, created_at, address)
SELECT 'cfdd50ad-928b-47a8-8bee-2ecb4f89baba', 'Cafea gratuita',
       'Tiny Cup Specialty Coffee', 'Un voucher pentru o cafea gratuita.',
       100, 30, 1, 0, NOW(), 'Strada Geoagiu 6, București'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM nev_rewards WHERE id = 'cfdd50ad-928b-47a8-8bee-2ecb4f89baba');

INSERT INTO nev_rewards
    (id, title, partner_name, description, points_cost, stock, active, version, created_at, address)
SELECT 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Reducere 15% carte',
       'Cărturești', '15% reducere la orice carte din librarie.',
       80, NULL, 1, 0, NOW(), 'Calea Victoriei 45, București'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM nev_rewards WHERE id = 'a1b2c3d4-e5f6-7890-abcd-ef1234567890');

INSERT INTO nev_rewards
    (id, title, partner_name, description, points_cost, stock, active, version, created_at, address)
SELECT 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Discount 10% vinyl',
       'Bazar de Muzică', '10% reducere la viniluri si accesorii muzicale.',
       60, NULL, 1, 0, NOW(), 'Strada Mendeleev 8, București'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM nev_rewards WHERE id = 'b2c3d4e5-f6a7-8901-bcde-f12345678901');

INSERT INTO nev_rewards
    (id, title, partner_name, description, points_cost, stock, active, version, created_at, address)
SELECT 'c3d4e5f6-a7b8-9012-cdef-123456789012', 'Print gratuit A4',
       'Mibe Print', 'Un print gratuit A4 color sau alb-negru.',
       50, 50, 1, 0, NOW(), 'Calea București 2bis, Balotești'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM nev_rewards WHERE id = 'c3d4e5f6-a7b8-9012-cdef-123456789012');

-- ── Test redemption for Serban (only if the seed user exists) ────────────────
-- NOTE: snapshot value below mirrors the original Java seeder; it does not
-- deduct from the user's available_points (kept at 120) on purpose.
INSERT INTO nev_reward_redemptions
    (id, reward_id, user_id, reward_title, points_spent, redemption_code,
     redeemed_at, user_available_points_after_redemption)
SELECT 'd4e5f6a7-b8c9-0123-defa-234567890123',
       'cfdd50ad-928b-47a8-8bee-2ecb4f89baba',
       '11111111-1111-1111-1111-111111111111',
       'Cafea gratuita', 100, 'NEV-TEST-2025-001',
       '2026-06-05 12:00:00', 20
FROM DUAL
WHERE EXISTS (SELECT 1 FROM nev_users WHERE id = '11111111-1111-1111-1111-111111111111')
  AND NOT EXISTS (SELECT 1 FROM nev_reward_redemptions
                  WHERE id = 'd4e5f6a7-b8c9-0123-defa-234567890123');

-- ── Event (formerly bootstrap "Neverest Community Run") ──────────────────────
INSERT INTO nev_events
    (id, title, activity_type, location, starts_at, points_reward,
     description, recurrence, route_map_url, strava_club_url, whatsapp_group_url, created_at)
SELECT '44444444-4444-4444-4444-444444444444', 'Neverest Community Run',
       'RUNNING', 'Parcul Herastrau, Bucuresti', '2026-06-15 09:00:00', 80,
       NULL, 'NONE', NULL, NULL, NULL, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM nev_events WHERE id = '44444444-4444-4444-4444-444444444444');

-- ── Challenge (formerly bootstrap "7K Weekly Run") ───────────────────────────
INSERT INTO nev_challenges
    (id, title, description, activity_type, mode, frequency,
     starts_at, ends_at, points_reward, target_value, target_unit, created_at)
SELECT '55555555-5555-5555-5555-555555555555', '7K Weekly Run',
       'Alearga minimum 7 km in aceasta saptamana.',
       'RUNNING', 'ONLINE', 'WEEKLY',
       '2026-06-07 00:00:00', '2026-06-28 23:59:00', 120, 7.0, 'km', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM nev_challenges WHERE id = '55555555-5555-5555-5555-555555555555');
