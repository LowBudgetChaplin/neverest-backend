-- =============================================================
-- Neverest - script de setup MySQL 8.x
--
-- Acest script:
--   1) (Optional) Sterge tabelele "vechi" din schema initiala
--      (app_users, events, ..., admin_audit_logs) ca sa eviti
--      conflicte cu schema oficiala a aplicatiei.
--   2) Creeaza database-ul neverest (daca nu exista) cu utf8mb4.
--   3) Creeaza schema oficiala "nev_*", identica cu cea pe care
--      o asteapta entitatile JPA si Flyway (V1__init_schema.sql).
--
-- Recomandare: ruleaza scriptul in MySQL Workbench cu un user
-- care are privilegii pe DB "neverest" (de ex. root sau
-- neverest_app dupa ce ai facut GRANT).
-- =============================================================

CREATE DATABASE IF NOT EXISTS neverest
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE neverest;

-- -------------------------------------------------------------
-- (Optional) Curatenie a schemei initiale (app_users etc.)
-- Decomenteaza daca vrei sa stergi tabelele "vechi" creeate
-- manual din 01_schema.sql. Aplicatia NU le foloseste.
-- -------------------------------------------------------------
-- SET FOREIGN_KEY_CHECKS = 0;
-- DROP TABLE IF EXISTS admin_audit_logs;
-- DROP TABLE IF EXISTS user_authorities;
-- DROP TABLE IF EXISTS user_activity_points;
-- DROP TABLE IF EXISTS reward_redemptions;
-- DROP TABLE IF EXISTS rewards;
-- DROP TABLE IF EXISTS challenge_submissions;
-- DROP TABLE IF EXISTS challenges;
-- DROP TABLE IF EXISTS event_check_ins;
-- DROP TABLE IF EXISTS events;
-- DROP TABLE IF EXISTS app_users;
-- SET FOREIGN_KEY_CHECKS = 1;

-- -------------------------------------------------------------
-- Schema oficiala (1:1 cu entitatile JPA)
-- -------------------------------------------------------------

CREATE TABLE IF NOT EXISTS nev_users (
    id CHAR(36) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    qr_code VARCHAR(32) NOT NULL,
    auth_subject VARCHAR(200) NULL,
    total_points INT NOT NULL,
    available_points INT NOT NULL,
    points_padel INT NOT NULL,
    points_mountain INT NOT NULL,
    points_running INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_nev_users_qr_code UNIQUE (qr_code),
    CONSTRAINT uk_nev_users_auth_subject UNIQUE (auth_subject),
    INDEX idx_nev_users_display_name (display_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nev_events (
    id CHAR(36) NOT NULL,
    title VARCHAR(180) NOT NULL,
    activity_type VARCHAR(32) NOT NULL,
    location VARCHAR(200) NOT NULL,
    starts_at DATETIME(6) NOT NULL,
    points_reward INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_nev_events_starts_at (starts_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nev_event_checkins (
    id CHAR(36) NOT NULL,
    event_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    checked_in_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_nev_event_checkins_event_user UNIQUE (event_id, user_id),
    INDEX idx_nev_event_checkins_event (event_id),
    INDEX idx_nev_event_checkins_user (user_id),
    CONSTRAINT fk_nev_event_checkins_event FOREIGN KEY (event_id) REFERENCES nev_events(id),
    CONSTRAINT fk_nev_event_checkins_user FOREIGN KEY (user_id) REFERENCES nev_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nev_challenges (
    id CHAR(36) NOT NULL,
    title VARCHAR(180) NOT NULL,
    description VARCHAR(600) NOT NULL,
    activity_type VARCHAR(32) NOT NULL,
    mode VARCHAR(32) NOT NULL,
    frequency VARCHAR(32) NOT NULL,
    starts_at DATETIME(6) NULL,
    ends_at DATETIME(6) NULL,
    points_reward INT NOT NULL,
    target_value DOUBLE NULL,
    target_unit VARCHAR(64) NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_nev_challenges_starts_at (starts_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nev_challenge_submissions (
    id CHAR(36) NOT NULL,
    challenge_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    proof_text VARCHAR(1000) NULL,
    metric_value DOUBLE NULL,
    submitted_at DATETIME(6) NOT NULL,
    status VARCHAR(32) NOT NULL,
    awarded_points INT NOT NULL,
    reviewed_at DATETIME(6) NULL,
    reviewer_note VARCHAR(600) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_nev_challenge_submissions_challenge_user UNIQUE (challenge_id, user_id),
    INDEX idx_nev_challenge_submissions_challenge (challenge_id),
    INDEX idx_nev_challenge_submissions_user (user_id),
    CONSTRAINT fk_nev_challenge_submissions_challenge FOREIGN KEY (challenge_id) REFERENCES nev_challenges(id),
    CONSTRAINT fk_nev_challenge_submissions_user FOREIGN KEY (user_id) REFERENCES nev_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nev_rewards (
    id CHAR(36) NOT NULL,
    title VARCHAR(180) NOT NULL,
    partner_name VARCHAR(180) NOT NULL,
    description VARCHAR(700) NOT NULL,
    points_cost INT NOT NULL,
    stock INT NULL,
    active BIT NOT NULL,
    version BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_nev_rewards_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nev_reward_redemptions (
    id CHAR(36) NOT NULL,
    reward_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    reward_title VARCHAR(180) NOT NULL,
    points_spent INT NOT NULL,
    redemption_code VARCHAR(40) NOT NULL,
    redeemed_at DATETIME(6) NOT NULL,
    user_available_points_after_redemption INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_nev_reward_redemptions_code UNIQUE (redemption_code),
    INDEX idx_nev_reward_redemptions_user (user_id),
    INDEX idx_nev_reward_redemptions_redeemed_at (redeemed_at),
    CONSTRAINT fk_nev_reward_redemptions_reward FOREIGN KEY (reward_id) REFERENCES nev_rewards(id),
    CONSTRAINT fk_nev_reward_redemptions_user FOREIGN KEY (user_id) REFERENCES nev_users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nev_audit_logs (
    id CHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    action VARCHAR(80) NOT NULL,
    actor VARCHAR(220) NOT NULL,
    success BIT NOT NULL,
    message VARCHAR(700) NOT NULL,
    metadata_encoded VARCHAR(4000) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_nev_audit_logs_created_at (created_at),
    INDEX idx_nev_audit_logs_action (action),
    INDEX idx_nev_audit_logs_actor (actor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nev_announcement_tasks (
    id CHAR(36) NOT NULL,
    event_id CHAR(36) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    payload VARCHAR(4000) NOT NULL,
    status VARCHAR(32) NOT NULL,
    attempt_count INT NOT NULL,
    max_attempts INT NOT NULL,
    next_attempt_at DATETIME(6) NOT NULL,
    last_attempt_at DATETIME(6) NULL,
    last_status_code INT NULL,
    last_error VARCHAR(700) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_nev_announcement_tasks_event_channel UNIQUE (event_id, channel),
    INDEX idx_nev_announcement_tasks_status_next_attempt (status, next_attempt_at),
    CONSTRAINT fk_nev_announcement_tasks_event FOREIGN KEY (event_id) REFERENCES nev_events(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- (Optional) User dedicat aplicatiei (recomandat in productie)
-- Modifica parola si decomenteaza daca vrei sa nu folosesti root.
-- =============================================================
-- CREATE USER IF NOT EXISTS 'neverest_app'@'localhost' IDENTIFIED BY 'CHANGE_ME';
-- GRANT ALL PRIVILEGES ON neverest.* TO 'neverest_app'@'localhost';
-- FLUSH PRIVILEGES;
