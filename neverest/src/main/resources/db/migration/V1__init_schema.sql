create table if not exists nev_users (
    id char(36) primary key,
    display_name varchar(120) not null,
    qr_code varchar(32) not null unique,
    auth_subject varchar(200) unique,
    total_points integer not null,
    available_points integer not null,
    points_padel integer not null,
    points_mountain integer not null,
    points_running integer not null,
    created_at timestamp not null
);

create index idx_nev_users_display_name on nev_users(display_name);

create table if not exists nev_events (
    id char(36) primary key,
    title varchar(180) not null,
    activity_type varchar(32) not null,
    location varchar(200) not null,
    starts_at timestamp not null,
    points_reward integer not null,
    created_at timestamp not null
);

create index idx_nev_events_starts_at on nev_events(starts_at);

create table if not exists nev_event_checkins (
    id char(36) primary key,
    event_id char(36) not null,
    user_id char(36) not null,
    checked_in_at timestamp not null,
    constraint fk_nev_event_checkins_event foreign key (event_id) references nev_events(id),
    constraint fk_nev_event_checkins_user foreign key (user_id) references nev_users(id),
    constraint uk_nev_event_checkins_event_user unique (event_id, user_id)
);

create index idx_nev_event_checkins_event on nev_event_checkins(event_id);
create index idx_nev_event_checkins_user on nev_event_checkins(user_id);

create table if not exists nev_challenges (
    id char(36) primary key,
    title varchar(180) not null,
    description varchar(600) not null,
    activity_type varchar(32) not null,
    mode varchar(32) not null,
    frequency varchar(32) not null,
    starts_at timestamp null,
    ends_at timestamp null,
    points_reward integer not null,
    target_value double precision null,
    target_unit varchar(64) null,
    created_at timestamp not null
);

create index idx_nev_challenges_starts_at on nev_challenges(starts_at);

create table if not exists nev_challenge_submissions (
    id char(36) primary key,
    challenge_id char(36) not null,
    user_id char(36) not null,
    proof_text varchar(1000) null,
    metric_value double precision null,
    submitted_at timestamp not null,
    status varchar(32) not null,
    awarded_points integer not null,
    reviewed_at timestamp null,
    reviewer_note varchar(600) null,
    constraint fk_nev_challenge_submissions_challenge foreign key (challenge_id) references nev_challenges(id),
    constraint fk_nev_challenge_submissions_user foreign key (user_id) references nev_users(id),
    constraint uk_nev_challenge_submissions_challenge_user unique (challenge_id, user_id)
);

create index idx_nev_challenge_submissions_challenge on nev_challenge_submissions(challenge_id);
create index idx_nev_challenge_submissions_user on nev_challenge_submissions(user_id);

create table if not exists nev_rewards (
    id char(36) primary key,
    title varchar(180) not null,
    partner_name varchar(180) not null,
    description varchar(700) not null,
    points_cost integer not null,
    stock integer null,
    active boolean not null,
    version bigint not null,
    created_at timestamp not null
);

create index idx_nev_rewards_title on nev_rewards(title);

create table if not exists nev_reward_redemptions (
    id char(36) primary key,
    reward_id char(36) not null,
    user_id char(36) not null,
    reward_title varchar(180) not null,
    points_spent integer not null,
    redemption_code varchar(40) not null unique,
    redeemed_at timestamp not null,
    user_available_points_after_redemption integer not null,
    constraint fk_nev_reward_redemptions_reward foreign key (reward_id) references nev_rewards(id),
    constraint fk_nev_reward_redemptions_user foreign key (user_id) references nev_users(id)
);

create index idx_nev_reward_redemptions_user on nev_reward_redemptions(user_id);
create index idx_nev_reward_redemptions_redeemed_at on nev_reward_redemptions(redeemed_at);

create table if not exists nev_audit_logs (
    id char(36) primary key,
    created_at timestamp not null,
    action varchar(80) not null,
    actor varchar(220) not null,
    success boolean not null,
    message varchar(700) not null,
    metadata_encoded varchar(4000) not null
);

create index idx_nev_audit_logs_created_at on nev_audit_logs(created_at);
create index idx_nev_audit_logs_action on nev_audit_logs(action);
create index idx_nev_audit_logs_actor on nev_audit_logs(actor);

create table if not exists nev_announcement_tasks (
    id char(36) primary key,
    event_id char(36) not null,
    channel varchar(32) not null,
    payload varchar(4000) not null,
    status varchar(32) not null,
    attempt_count integer not null,
    max_attempts integer not null,
    next_attempt_at timestamp not null,
    last_attempt_at timestamp null,
    last_status_code integer null,
    last_error varchar(700) null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint fk_nev_announcement_tasks_event foreign key (event_id) references nev_events(id),
    constraint uk_nev_announcement_tasks_event_channel unique (event_id, channel)
);

create index idx_nev_announcement_tasks_status_next_attempt
    on nev_announcement_tasks(status, next_attempt_at);
