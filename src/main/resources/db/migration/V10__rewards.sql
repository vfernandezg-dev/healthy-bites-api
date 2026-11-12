-- Módulo rewards: recompensas de los nutricionistas (desbloquean un contenido o un plan, nunca los dos) y las rachas
-- de días seguidos registrando hábitos que cada cliente lleva hacia ellas.

create table rewards
(
    id                   uuid primary key      default uuidv7(),
    author_id            uuid         not null references users (id),
    title                varchar(120) not null,
    description          varchar(500) not null,
    required_streak_days integer      not null,
    content_item_id      uuid references content_items (id),
    meal_plan_id         uuid references meal_plans (id),
    active               boolean      not null default true,
    created_at           timestamptz  not null default now(),
    updated_at           timestamptz  not null default now(),
    version              bigint       not null default 0,
    constraint ck_rewards_days check (required_streak_days between 1 and 365),
    constraint ck_rewards_target check ((content_item_id is null) <> (meal_plan_id is null))
);

create index ix_rewards_author on rewards (author_id);

create table reward_streaks
(
    id              uuid primary key     default uuidv7(),
    reward_id       uuid        not null references rewards (id) on delete cascade,
    client_id       uuid        not null references users (id) on delete cascade,
    current_days    integer     not null default 0,
    last_counted_on date,
    completed_at    timestamptz,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    version         bigint      not null default 0,
    constraint ux_reward_streaks unique (reward_id, client_id),
    constraint ck_reward_streaks_days check (current_days >= 0)
);

create index ix_reward_streaks_client on reward_streaks (client_id);
