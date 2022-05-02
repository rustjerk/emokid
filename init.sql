drop table if exists music_bands;
drop table if exists auth_tokens;
drop table if exists users;

create table users
(
    username text primary key,
    password text,
    salt     text
);

create table auth_tokens
(
    username text primary key,
    token    text
);

create table music_bands
(
    id               bigserial primary key,
    owner            text             not null,
    name             text             not null,
    coord_x          double precision not null,
    coord_y          bigint           not null,
    creation_date    timestamptz      not null,
    num_participants int              not null,
    description      text             not null,
    genre            text,
    studio_name      text,
    studio_address   text
);