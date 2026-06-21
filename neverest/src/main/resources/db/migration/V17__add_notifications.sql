-- V17: In-app notifications.
-- Each row is one notification addressed to a single recipient user.
-- "is_read" drives the unread badge on the bell icon; it is flipped to true
-- when the user opens the notifications screen (mark-all-read).

create table if not exists nev_notifications (
    id char(36) primary key,
    user_id char(36) not null,
    type varchar(40) not null,
    title varchar(200) not null,
    body varchar(600) not null,
    is_read boolean not null,
    challenge_id char(36) null,
    submission_id char(36) null,
    created_at timestamp not null,
    constraint fk_nev_notifications_user foreign key (user_id) references nev_users(id)
);

create index idx_nev_notifications_user_created on nev_notifications(user_id, created_at);
create index idx_nev_notifications_user_unread on nev_notifications(user_id, is_read);
