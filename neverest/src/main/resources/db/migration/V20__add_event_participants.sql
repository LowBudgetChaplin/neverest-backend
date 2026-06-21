create table if not exists nev_event_participants (
    id char(36) primary key,
    event_id char(36) not null,
    user_id char(36) not null,
    joined_at timestamp not null,
    constraint uk_nev_event_participants_event_user unique (event_id, user_id),
    constraint fk_nev_event_participants_event foreign key (event_id) references nev_events(id),
    constraint fk_nev_event_participants_user foreign key (user_id) references nev_users(id)
);

create index idx_nev_event_participants_event on nev_event_participants(event_id);
create index idx_nev_event_participants_user on nev_event_participants(user_id);
