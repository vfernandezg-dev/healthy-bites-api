-- Altas de nutricionistas por invitación (el registro público es solo para clientes). Como el resto de tokens,
-- solo se guarda el hash; caduca y sirve una vez.
create table nutritionist_invitations
(
    id               uuid primary key     default uuidv7(),
    email            varchar(254) not null,
    first_name       varchar(60)  not null,
    last_name        varchar(60)  not null,
    token_hash       varchar(64)  not null,
    invited_by       uuid         not null references users (id),
    expires_at       timestamptz  not null,
    accepted_at      timestamptz,
    accepted_user_id uuid references users (id),
    revoked_at       timestamptz,
    created_at       timestamptz  not null default now(),
    updated_at       timestamptz  not null default now(),
    version          bigint       not null default 0,
    constraint ux_nutritionist_invitations_token unique (token_hash)
);

create index ix_nutritionist_invitations_email on nutritionist_invitations (lower(email));
