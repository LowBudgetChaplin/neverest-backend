-- Neverest schema for MySQL 8.x
-- Acest script este aplicat automat de Flyway la prima pornire a backend-ului
-- pe o baza MySQL goala. El reflecta 1:1 entitatile JPA din com.app.neverest.persistence.entity.*

create table if not exists nev_users (
    id char(36) not null,
    display_name varchar(120) not null,
    qr_code varchar(32) not null,
    auth_subject varchar(200) null,
    total_points int not null,
    available_points int not null,
    points_padel int not null,
    points_mountain int not null,
    points_running int not null,
    created_at datetime(6) not null,
    primary key (id),
    constraint uk_nev_users_qr_code unique (qr_code),
    constraint uk_nev_users_auth_subject unique (auth_subject),
    index idx_nev_users_display_name (display_name)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table if not exists nev_events (
    id char(36) not null,
    title varchar(180) not null,
    activity_type varchar(32) not null,
    location varchar(200) not null,
    starts_at datetime(6) not null,
    points_reward int not null,
    created_at datetime(6) not null,
    primary key (id),
    index idx_nev_events_starts_at (starts_at)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table if not exists nev_event_checkins (
    id char(36) not null,
    event_id char(36) not null,
    user_id char(36) not null,
    checked_in_at datetime(6) not null,
    primary key (id),
    constraint uk_nev_event_checkins_event_user unique (event_id, user_id),
    index idx_nev_event_checkins_event (event_id),
    index idx_nev_event_checkins_user (user_id),
    constraint fk_nev_event_checkins_event foreign key (event_id) references nev_events(id),
    constraint fk_nev_event_checkins_user foreign key (user_id) references nev_users(id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table if not exists nev_challenges (
    id char(36) not null,
    title varchar(180) not null,
    description varchar(600) not null,
    activity_type varchar(32) not null,
    mode varchar(32) not null,
    frequency varchar(32) not null,
    starts_at datetime(6) null,
    ends_at datetime(6) null,
    points_reward int not null,
    target_value double null,
    target_unit varchar(64) null,
    created_at datetime(6) not null,
    primary key (id),
    index idx_nev_challenges_starts_at (starts_at)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table if not exists nev_challenge_submissions (
    id char(36) not null,
    challenge_id char(36) not null,
    user_id char(36) not null,
    proof_text varchar(1000) null,
    metric_value double null,
    submitted_at datetime(6) not null,
    status varchar(32) not null,
    awarded_points int not null,
    reviewed_at datetime(6) null,
    reviewer_note varchar(600) null,
    primary key (id),
    constraint uk_nev_challenge_submissions_challenge_user unique (challenge_id, user_id),
    index idx_nev_challenge_submissions_challenge (challenge_id),
    index idx_nev_challenge_submissions_user (user_id),
    constraint fk_nev_challenge_submissions_challenge foreign key (challenge_id) references nev_challenges(id),
    constraint fk_nev_challenge_submissions_user foreign key (user_id) references nev_users(id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table if not exists nev_rewards (
    id char(36) not null,
    title varchar(180) not null,
    partner_name varchar(180) not null,
    description varchar(700) not null,
    points_cost int not null,
    stock int null,
    active bit not null,
    version bigint not null,
    created_at datetime(6) not null,
    primary key (id),
    index idx_nev_rewards_title (title)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table if not exists nev_reward_redemptions (
    id char(36) not null,
    reward_id char(36) not null,
    user_id char(36) not null,
    reward_title varchar(180) not null,
    points_spent int not null,
    redemption_code varchar(40) not null,
    redeemed_at datetime(6) not null,
    user_available_points_after_redemption int not null,
    primary key (id),
    constraint uk_nev_reward_redemptions_code unique (redemption_code),
    index idx_nev_reward_redemptions_user (user_id),
    index idx_nev_reward_redemptions_redeemed_at (redeemed_at),
    constraint fk_nev_reward_redemptions_reward foreign key (reward_id) references nev_rewards(id),
    constraint fk_nev_reward_redemptions_user foreign key (user_id) references nev_users(id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table if not exists nev_audit_logs (
    id char(36) not null,
    created_at datetime(6) not null,
    action varchar(80) not null,
    actor varchar(220) not null,
    success bit not null,
    message varchar(700) not null,
    metadata_encoded varchar(4000) not null,
    primary key (id),
    index idx_nev_audit_logs_created_at (created_at),
    index idx_nev_audit_logs_action (action),
    index idx_nev_audit_logs_actor (actor)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table if not exists nev_announcement_tasks (
    id char(36) not null,
    event_id char(36) not null,
    channel varchar(32) not null,
    payload varchar(4000) not null,
    status varchar(32) not null,
    attempt_count int not null,
    max_attempts int not null,
    next_attempt_at datetime(6) not null,
    last_attempt_at datetime(6) null,
    last_status_code int null,
    last_error varchar(700) null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    primary key (id),
    constraint uk_nev_announcement_tasks_event_channel unique (event_id, channel),
    index idx_nev_announcement_tasks_status_next_attempt (status, next_attempt_at),
    constraint fk_nev_announcement_tasks_event foreign key (event_id) references nev_events(id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;
