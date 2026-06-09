-- V9: Add optional capacity (max participants) to nev_events.
-- NULL means unlimited; a positive value caps the number of check-ins.
ALTER TABLE nev_events
    ADD COLUMN capacity INT NULL AFTER points_reward;
