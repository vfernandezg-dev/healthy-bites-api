-- Módulo billing: planes (con su precio en la base de datos, no en el código), suscripciones y pagos.

create table subscription_plans
(
    id             uuid primary key        default uuidv7(),
    tier           varchar(10)    not null,
    name           varchar(60)    not null,
    description    varchar(500)   not null,
    price_amount   numeric(10, 2) not null,
    price_currency varchar(3)     not null,
    period_days    integer        not null default 30,
    active         boolean        not null default true,
    created_at     timestamptz    not null default now(),
    updated_at     timestamptz    not null default now(),
    version        bigint         not null default 0,
    constraint ux_subscription_plans_tier unique (tier),
    constraint ck_subscription_plans_tier check (tier in ('BASIC', 'PREMIUM', 'VIP')),
    constraint ck_subscription_plans_price check (price_amount >= 0),
    constraint ck_subscription_plans_period check (period_days between 1 and 366)
);

insert into subscription_plans (tier, name, description, price_amount, price_currency)
values ('BASIC', 'Básico', 'Metas, pesajes, hábitos, rachas y comunidad, más el contenido y los planes gratuitos.', 0.00,
        'USD'),
       ('PREMIUM', 'Premium', 'Todo lo del plan Básico, más el contenido y los planes de alimentación premium.', 5.99,
        'USD'),
       ('VIP', 'VIP', 'Todo lo de Premium, más el contenido y los planes exclusivos VIP.', 9.99, 'USD');

create table subscriptions
(
    id                   uuid primary key     default uuidv7(),
    user_id              uuid        not null references users (id) on delete cascade,
    plan_id              uuid        not null references subscription_plans (id),
    tier                 varchar(10) not null,
    status               varchar(20) not null,
    current_period_start timestamptz,
    current_period_end   timestamptz,
    canceled_at          timestamptz,
    created_at           timestamptz not null default now(),
    updated_at           timestamptz not null default now(),
    version              bigint      not null default 0,
    constraint ck_subscriptions_tier check (tier in ('PREMIUM', 'VIP')),
    constraint ck_subscriptions_status check (status in ('PENDING_PAYMENT', 'ACTIVE', 'CANCELED', 'EXPIRED'))
);

-- Como mucho una suscripción ACTIVE por usuario.
create unique index ux_subscriptions_one_active on subscriptions (user_id) where status = 'ACTIVE';
create index ix_subscriptions_user on subscriptions (user_id);

create table payments
(
    id                  uuid primary key        default uuidv7(),
    subscription_id     uuid           not null references subscriptions (id) on delete cascade,
    user_id             uuid           not null references users (id) on delete cascade,
    provider            varchar(20)    not null default 'PAYPAL',
    provider_order_id   varchar(64)    not null,
    provider_capture_id varchar(64),
    amount              numeric(10, 2) not null,
    currency            varchar(3)     not null,
    status              varchar(20)    not null,
    captured_at         timestamptz,
    created_at          timestamptz    not null default now(),
    updated_at          timestamptz    not null default now(),
    version             bigint         not null default 0,
    constraint ux_payments_provider_order unique (provider_order_id),
    constraint ck_payments_status check (status in ('CREATED', 'CAPTURED', 'FAILED', 'CANCELED'))
);

create index ix_payments_user on payments (user_id);
create index ix_payments_subscription on payments (subscription_id);
