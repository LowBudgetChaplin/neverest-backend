-- V15: Partner-owned challenges with a non-points benefit reward.
-- owner_user_id NULL = admin challenge (points). Non-null = partner challenge.
ALTER TABLE nev_challenges
    ADD COLUMN owner_user_id CHAR(36)     NULL AFTER target_unit,
    ADD COLUMN reward_kind   VARCHAR(32)  NULL AFTER owner_user_id,
    ADD COLUMN reward_label  VARCHAR(200) NULL AFTER reward_kind,
    ADD COLUMN brand         VARCHAR(120) NULL AFTER reward_label;

CREATE INDEX idx_nev_challenges_owner ON nev_challenges(owner_user_id);
