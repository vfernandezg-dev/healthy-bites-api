-- Registro de eventos de Spring Modulith (esquema v2 de spring-modulith-events-jdbc para PostgreSQL).
-- Cada evento publicado entre módulos se guarda aquí hasta que su listener termina; si la aplicación se cae a
-- mitad, se reintenta al arrancar. Con completion-mode=delete, los completados se borran.
create table event_publication
(
    id                     uuid                     not null primary key,
    listener_id            text                     not null,
    event_type             text                     not null,
    serialized_event       text                     not null,
    publication_date       timestamp with time zone not null,
    completion_date        timestamp with time zone,
    status                 text,
    completion_attempts    int,
    last_resubmission_date timestamp with time zone
);

create index event_publication_serialized_event_hash_idx on event_publication using hash (serialized_event);
create index event_publication_by_completion_date_idx on event_publication (completion_date);
