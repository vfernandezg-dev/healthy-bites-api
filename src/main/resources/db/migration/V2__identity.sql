-- Módulo identity: cuentas, sesiones (refresh tokens) y enlaces de restablecimiento de contraseña.
-- Convenciones: tablas en plural, PK UUIDv7, fechas como timestamptz (UTC), enums como varchar + CHECK y
-- created_at/updated_at/version en los agregados que cambian.

create table users
(
    id            uuid primary key     default uuidv7(),
    email         varchar(254) not null,
    password_hash varchar(100) not null,
    role          varchar(20)  not null,
    first_name    varchar(60)  not null,
    last_name     varchar(60)  not null,
    status        varchar(20)  not null default 'ACTIVE',
    last_login_at timestamptz,
    created_at    timestamptz  not null default now(),
    updated_at    timestamptz  not null default now(),
    version       bigint       not null default 0,
    constraint ck_users_role check (role in ('ADMIN', 'CLIENT', 'NUTRITIONIST')),
    constraint ck_users_status check (status in ('ACTIVE', 'DEACTIVATED'))
);

-- Un correo, una cuenta, sin distinguir mayúsculas.
create unique index ux_users_email on users (lower(email));

-- Sesiones: solo se guarda el hash SHA-256 del refresh token. Cada rotación crea un token nuevo en la misma
-- familia (la sesión); reutilizar uno rotado revoca la familia entera.
create table refresh_tokens
(
    id                uuid primary key default uuidv7(),
    user_id           uuid        not null references users (id) on delete cascade,
    family_id         uuid        not null,
    token_hash        varchar(64) not null,
    created_at        timestamptz not null,
    expires_at        timestamptz not null,
    family_expires_at timestamptz not null,
    revoked_at        timestamptz,
    replaced_by_id    uuid,
    constraint ux_refresh_tokens_hash unique (token_hash)
);

create index ix_refresh_tokens_family on refresh_tokens (family_id);
create index ix_refresh_tokens_user on refresh_tokens (user_id);

-- Enlaces para restablecer la contraseña: hash del token, caducidad corta y un solo uso.
create table password_reset_tokens
(
    id         uuid primary key default uuidv7(),
    user_id    uuid        not null references users (id) on delete cascade,
    token_hash varchar(64) not null,
    created_at timestamptz not null,
    expires_at timestamptz not null,
    used_at    timestamptz,
    constraint ux_password_reset_tokens_hash unique (token_hash)
);

create index ix_password_reset_tokens_user on password_reset_tokens (user_id);
