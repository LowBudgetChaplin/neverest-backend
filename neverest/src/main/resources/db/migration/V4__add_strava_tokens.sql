create table if not exists nev_strava_tokens (
    id char(36) primary key,
    user_id char(36) not null unique,
    athlete_id bigint not null,
    athlete_name varchar(200),
    athlete_city varchar(200),
    access_token varchar(300) not null,
    refresh_token varchar(300) not null,
    expires_at bigint not null,
    scope varchar(200),
    connected_at timestamp not null,
    constraint fk_nev_strava_tokens_user foreign key (user_id) references nev_users(id)
);

create index idx_nev_strava_tokens_user on nev_strava_tokens(user_id);
create index idx_nev_strava_tokens_athlete on nev_strava_tokens(athlete_id);
