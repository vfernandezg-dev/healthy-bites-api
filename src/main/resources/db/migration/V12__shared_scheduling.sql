-- Bloqueos de las tareas programadas (ShedLock): con varias instancias, cada tarea corre en una sola a la vez.
-- La hora es la de la base de datos (UTC), no la de cada instancia, así que un reloj desajustado no rompe el bloqueo.
create table shedlock
(
    name       varchar(64)  not null primary key,
    lock_until timestamp    not null,
    locked_at  timestamp    not null,
    locked_by  varchar(255) not null
);
