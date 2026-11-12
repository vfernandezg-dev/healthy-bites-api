-- Módulo progress: metas de peso con su historial de estados, pesajes (uno por día y meta) y registros diarios de
-- hábitos (uno por día y cliente, notas de 0 a 10).

create table goals
(
    id               uuid primary key       default uuidv7(),
    client_id        uuid          not null references users (id) on delete cascade,
    title            varchar(120)  not null,
    description      varchar(500),
    target_weight_kg numeric(5, 1) not null,
    status           varchar(20)   not null,
    started_on       date          not null,
    completed_at     timestamptz,
    created_at       timestamptz   not null default now(),
    updated_at       timestamptz   not null default now(),
    version          bigint        not null default 0,
    constraint ck_goals_target check (target_weight_kg between 20 and 400),
    constraint ck_goals_status check (status in ('ACTIVE', 'COMPLETED', 'ABANDONED'))
);

-- Una meta activa por cliente.
create unique index ux_goals_one_active on goals (client_id) where status = 'ACTIVE';
create index ix_goals_client on goals (client_id);

create table goal_status_changes
(
    id          uuid primary key     default uuidv7(),
    goal_id     uuid        not null references goals (id) on delete cascade,
    from_status varchar(20),
    to_status   varchar(20) not null,
    changed_at  timestamptz not null default now()
);

create index ix_goal_status_changes_goal on goal_status_changes (goal_id);

create table weight_entries
(
    id          uuid primary key       default uuidv7(),
    goal_id     uuid          not null references goals (id) on delete cascade,
    measured_on date          not null,
    weight_kg   numeric(5, 1) not null,
    notes       varchar(500),
    created_at  timestamptz   not null default now(),
    updated_at  timestamptz   not null default now(),
    version     bigint        not null default 0,
    constraint ux_weight_entries_day unique (goal_id, measured_on),
    constraint ck_weight_entries_weight check (weight_kg between 20 and 400)
);

create table habit_logs
(
    id         uuid primary key     default uuidv7(),
    client_id  uuid        not null references users (id) on delete cascade,
    logged_on  date        not null,
    hydration  integer     not null,
    nutrition  integer     not null,
    exercise   integer     not null,
    sleep      integer     not null,
    notes      varchar(500),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    version    bigint      not null default 0,
    constraint ux_habit_logs_day unique (client_id, logged_on),
    constraint ck_habit_logs_scores check (hydration between 0 and 10 and nutrition between 0 and 10
        and exercise between 0 and 10 and sleep between 0 and 10)
);
