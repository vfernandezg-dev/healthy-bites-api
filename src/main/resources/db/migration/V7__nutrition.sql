-- Módulo nutrition: planes de alimentación de los nutricionistas, sus comidas por día y las inscripciones de los
-- clientes (por su cuenta o desbloqueadas con una recompensa).

create table meal_plans
(
    id            uuid primary key       default uuidv7(),
    author_id     uuid          not null references users (id),
    title         varchar(120)  not null,
    description   varchar(2000) not null,
    goal          varchar(20)   not null,
    duration_days integer       not null,
    required_tier varchar(10)   not null,
    status        varchar(20)   not null,
    published_at  timestamptz,
    created_at    timestamptz   not null default now(),
    updated_at    timestamptz   not null default now(),
    version       bigint        not null default 0,
    constraint ck_meal_plans_goal check (goal in ('WEIGHT_LOSS', 'MAINTENANCE', 'MUSCLE_GAIN')),
    constraint ck_meal_plans_duration check (duration_days between 1 and 365),
    constraint ck_meal_plans_tier check (required_tier in ('BASIC', 'PREMIUM', 'VIP')),
    constraint ck_meal_plans_status check (status in ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

create index ix_meal_plans_author on meal_plans (author_id);
create index ix_meal_plans_published on meal_plans (status, published_at);

create table meals
(
    id           uuid primary key      default uuidv7(),
    meal_plan_id uuid         not null references meal_plans (id) on delete cascade,
    day_number   integer      not null,
    position     integer      not null,
    category     varchar(20)  not null,
    name         varchar(120) not null,
    description  varchar(1000),
    calories     integer      not null,
    created_at   timestamptz  not null default now(),
    updated_at   timestamptz  not null default now(),
    version      bigint       not null default 0,
    constraint ux_meals_slot unique (meal_plan_id, day_number, position),
    constraint ck_meals_day check (day_number >= 1),
    constraint ck_meals_position check (position >= 0),
    constraint ck_meals_category check (category in ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK')),
    constraint ck_meals_calories check (calories between 0 and 10000)
);

create table meal_plan_enrollments
(
    id           uuid primary key     default uuidv7(),
    meal_plan_id uuid        not null references meal_plans (id) on delete cascade,
    client_id    uuid        not null references users (id) on delete cascade,
    source       varchar(20) not null,
    enrolled_at  timestamptz not null default now(),
    constraint ux_meal_plan_enrollments unique (meal_plan_id, client_id),
    constraint ck_meal_plan_enrollments_source check (source in ('SELF', 'REWARD'))
);

create index ix_meal_plan_enrollments_client on meal_plan_enrollments (client_id);
