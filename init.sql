create table users
(
    username Utf8,
    password Utf8,
    salt     Utf8,
    primary key (username)
);

create table auth_tokens
(
    username Utf8,
    token    Utf8,
    primary key (username)
);

create table music_bands
(
    id               Uint64,
    owner            Utf8,
    name             Utf8,
    coord_x          Double,
    coord_y          Uint64,
    creation_date    Datetime,
    timezone         Utf8,
    num_participants Uint32,              
    description      Utf8,
    genre            Utf8,
    studio_name      Utf8,
    studio_address   Utf8,
    primary key (id)
);
