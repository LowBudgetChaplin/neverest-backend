alter table nev_users
    add column password_hash varchar(120);

alter table nev_users
    add column role varchar(24) not null default 'USER';

update nev_users
set role = 'USER'
where role is null or trim(role) = '';
