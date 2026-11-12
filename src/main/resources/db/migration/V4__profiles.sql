-- Módulo profiles: datos de cada rol. La PK es el id del usuario (un único id por persona); el nombre y el correo
-- viven en users (identity).

create table client_profiles
(
    user_id    uuid primary key references users (id) on delete cascade,
    sex        varchar(10),
    birth_date date,
    height_cm  numeric(5, 1),
    weight_kg  numeric(5, 1),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    version    bigint      not null default 0,
    constraint ck_client_profiles_sex check (sex in ('FEMALE', 'MALE', 'OTHER')),
    constraint ck_client_profiles_height check (height_cm between 50 and 260),
    constraint ck_client_profiles_weight check (weight_kg between 20 and 400)
);

create table nutritionist_profiles
(
    user_id    uuid primary key references users (id) on delete cascade,
    bio        varchar(2000),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    version    bigint      not null default 0
);
