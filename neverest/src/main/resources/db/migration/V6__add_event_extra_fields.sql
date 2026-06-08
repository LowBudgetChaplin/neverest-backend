-- V6: Add extra fields to nev_events (description, recurrence, route map, social links)
ALTER TABLE nev_events
    ADD COLUMN description    VARCHAR(700)  NULL AFTER points_reward,
    ADD COLUMN recurrence     VARCHAR(32)   NOT NULL DEFAULT 'NONE' AFTER description,
    ADD COLUMN route_map_url  VARCHAR(500)  NULL AFTER recurrence,
    ADD COLUMN strava_club_url VARCHAR(300) NULL AFTER route_map_url,
    ADD COLUMN whatsapp_group_url VARCHAR(300) NULL AFTER strava_club_url;

-- Seed: real trail running event linked to test Strava club + WhatsApp group
INSERT INTO nev_events (
    id, title, activity_type, location, starts_at, points_reward,
    description, recurrence, route_map_url, strava_club_url, whatsapp_group_url,
    created_at
) VALUES (
    'e1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b5c6',
    'Trail Bucegi – Vârful Omu',
    'RUNNING',
    'Bușteni, Prahova',
    '2026-06-20 07:00:00',
    300,
    'Traseu circular de 22 km cu 1600 m diferență de nivel. Punct de întâlnire: Cabana Caraiman. Ritm moderat, echipament de munte obligatoriu.',
    'MONTHLY',
    'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d11220.123456789!2d25.4500!3d45.4450!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x0%3A0x0!2zVsOicmZ1bCBPbXU!5e0!3m2!1sro!2sro!4v1234567890',
    'https://www.strava.com/clubs/neverest-test',
    'https://chat.whatsapp.com/neverest-test-group',
    NOW()
);
