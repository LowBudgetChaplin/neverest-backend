

alter table nev_rewards add column owner_user_id char(36) null;

create index idx_nev_rewards_owner on nev_rewards(owner_user_id);
