-- Límites de peticiones compartidos por todas las instancias (JdbcRateLimiter): un evento por fila. El limitador
-- borra los de cada clave que salen de su ventana y RateLimitCleanupJob los de más de un día.
create table rate_limit_events
(
    limiter     text                     not null,
    key         text                     not null,
    occurred_at timestamp with time zone not null
);

create index ix_rate_limit_events_key on rate_limit_events (limiter, key, occurred_at desc);
create index ix_rate_limit_events_occurred_at on rate_limit_events (occurred_at);
