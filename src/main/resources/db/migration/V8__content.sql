-- Módulo content: artículos, vídeos y guías de los nutricionistas. La portada es pública; el fichero (vídeo, PDF),
-- protegido. Los desbloqueos dan acceso a un contenido de pago sin la suscripción (por ejemplo, una recompensa).

create table content_items
(
    id             uuid primary key      default uuidv7(),
    author_id      uuid         not null references users (id),
    slug           varchar(140) not null,
    title          varchar(120) not null,
    summary        varchar(500) not null,
    body           text,
    type           varchar(10)  not null,
    category       varchar(20)  not null,
    required_tier  varchar(10)  not null,
    cover_media_id uuid references media_files (id),
    file_media_id  uuid references media_files (id),
    status         varchar(20)  not null,
    published_at   timestamptz,
    created_at     timestamptz  not null default now(),
    updated_at     timestamptz  not null default now(),
    version        bigint       not null default 0,
    constraint ux_content_items_slug unique (slug),
    constraint ck_content_items_type check (type in ('ARTICLE', 'VIDEO', 'GUIDE')),
    constraint ck_content_items_category check (category in ('NUTRITION', 'EXERCISE', 'MENTAL_HEALTH')),
    constraint ck_content_items_tier check (required_tier in ('BASIC', 'PREMIUM', 'VIP')),
    constraint ck_content_items_status check (status in ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

create index ix_content_items_author on content_items (author_id);
create index ix_content_items_published on content_items (status, published_at);

create table content_unlocks
(
    id              uuid primary key     default uuidv7(),
    content_item_id uuid        not null references content_items (id) on delete cascade,
    user_id         uuid        not null references users (id) on delete cascade,
    source          varchar(20) not null,
    unlocked_at     timestamptz not null default now(),
    constraint ux_content_unlocks unique (content_item_id, user_id),
    constraint ck_content_unlocks_source check (source in ('REWARD', 'ADMIN'))
);

create index ix_content_unlocks_user on content_unlocks (user_id);
