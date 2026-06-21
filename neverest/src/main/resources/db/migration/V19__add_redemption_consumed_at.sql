-- V19: One-time coupon validation by the partner.
-- consumed_at marks when the partner scanned/validated the code at the location.
-- A NULL value = code still valid; a set value = already used (next scan = expired).

alter table nev_reward_redemptions add column consumed_at timestamp null;
