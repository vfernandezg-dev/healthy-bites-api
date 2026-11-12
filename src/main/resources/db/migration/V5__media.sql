-- Módulo media: ficheros subidos. Solo se sirve lo registrado aquí, con el tipo detectado al subirlo (por sus
-- bytes). Los PUBLIC se leen por id; los PROTECTED (ficheros de contenido de pago) solo con una URL firmada.
create table media_files
(
    id           uuid primary key      default uuidv7(),
    owner_id     uuid         not null references users (id),
    purpose      varchar(20)  not null,
    visibility   varchar(10)  not null,
    content_type varchar(100) not null,
    size_bytes   bigint       not null,
    sha256       varchar(64)  not null,
    storage_key  varchar(100) not null,
    created_at   timestamptz  not null default now(),
    constraint ux_media_files_storage_key unique (storage_key),
    constraint ck_media_files_purpose check (purpose in ('CONTENT_COVER', 'CONTENT_FILE', 'POST_IMAGE')),
    constraint ck_media_files_visibility check (visibility in ('PUBLIC', 'PROTECTED')),
    constraint ck_media_files_size check (size_bytes > 0)
);

create index ix_media_files_owner on media_files (owner_id);
