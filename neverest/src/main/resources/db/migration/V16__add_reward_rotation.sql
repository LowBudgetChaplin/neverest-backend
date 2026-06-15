-- V16: Rotating one-time coupons. rotation_days = after how many days a user's
-- coupon for this reward becomes redeemable again (a brand-new code is issued on
-- the next redeem). NULL = one-time permanent (no rotation).
ALTER TABLE nev_rewards
    ADD COLUMN rotation_days INT NULL AFTER category;
