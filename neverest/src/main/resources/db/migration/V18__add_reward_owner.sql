-- V18: Partner-owned rewards.
-- Rewards now optionally belong to a partner (owner_user_id). NULL = platform
-- (admin) reward. Partner rewards live in the same global pool and use the same
-- points redemption flow; partners can only manage their own.

alter table nev_rewards add column owner_user_id char(36) null;

create index idx_nev_rewards_owner on nev_rewards(owner_user_id);
