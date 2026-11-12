-- Módulo community: grupos públicos y privados, membresías (las de grupos privados esperan aprobación),
-- publicaciones, comentarios y denuncias para moderar. El número de miembros se calcula, no se guarda.

create table community_groups
(
    id          uuid primary key     default uuidv7(),
    owner_id    uuid        not null references users (id),
    name        varchar(80) not null,
    description varchar(500),
    visibility  varchar(10) not null,
    created_at  timestamptz not null default now(),
    updated_at  timestamptz not null default now(),
    version     bigint      not null default 0,
    constraint ck_community_groups_visibility check (visibility in ('PUBLIC', 'PRIVATE'))
);

create table group_memberships
(
    id         uuid primary key     default uuidv7(),
    group_id   uuid        not null references community_groups (id) on delete cascade,
    user_id    uuid        not null references users (id) on delete cascade,
    role       varchar(10) not null,
    status     varchar(10) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    version    bigint      not null default 0,
    constraint ux_group_memberships unique (group_id, user_id),
    constraint ck_group_memberships_role check (role in ('OWNER', 'MEMBER')),
    constraint ck_group_memberships_status check (status in ('PENDING', 'ACTIVE'))
);

create index ix_group_memberships_user on group_memberships (user_id);

create table posts
(
    id             uuid primary key       default uuidv7(),
    group_id       uuid          not null references community_groups (id) on delete cascade,
    author_id      uuid          not null references users (id),
    title          varchar(120)  not null,
    body           varchar(4000) not null,
    image_media_id uuid references media_files (id),
    hidden_at      timestamptz,
    hidden_by      uuid references users (id),
    hidden_reason  varchar(200),
    created_at     timestamptz   not null default now(),
    updated_at     timestamptz   not null default now(),
    version        bigint        not null default 0
);

create index ix_posts_group on posts (group_id, created_at desc);

create table comments
(
    id            uuid primary key       default uuidv7(),
    post_id       uuid          not null references posts (id) on delete cascade,
    author_id     uuid          not null references users (id),
    body          varchar(1000) not null,
    hidden_at     timestamptz,
    hidden_by     uuid references users (id),
    hidden_reason varchar(200),
    created_at    timestamptz   not null default now(),
    updated_at    timestamptz   not null default now(),
    version       bigint        not null default 0
);

create index ix_comments_post on comments (post_id, created_at);

create table moderation_reports
(
    id          uuid primary key      default uuidv7(),
    reporter_id uuid         not null references users (id),
    post_id     uuid references posts (id) on delete cascade,
    comment_id  uuid references comments (id) on delete cascade,
    reason      varchar(500) not null,
    status      varchar(10)  not null,
    resolved_by uuid references users (id),
    resolved_at timestamptz,
    created_at  timestamptz  not null default now(),
    constraint ck_moderation_reports_target check ((post_id is null) <> (comment_id is null)),
    constraint ck_moderation_reports_status check (status in ('OPEN', 'RESOLVED', 'DISMISSED'))
);

create index ix_moderation_reports_status on moderation_reports (status, created_at);
