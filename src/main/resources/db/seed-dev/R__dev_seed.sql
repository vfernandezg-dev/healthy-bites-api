-- =====================================================================================================
-- Healthy Bites: datos de demostración (solo perfiles dev y test, nunca en producción).
--
-- Migración repetible de Flyway: se vuelve a aplicar cuando cambia este fichero. Es idempotente (ids fijos +
-- ON CONFLICT DO NOTHING), así que no pisa lo que se haya creado desde la aplicación. Cada módulo añade aquí sus
-- datos. No contiene datos personales reales ni referencias a ficheros locales.
--
-- Contraseña de todas las cuentas: healthy123
--   ADMIN          admin@healthybites.com
--   NUTRITIONIST   carlos.garcia@healthybites.com, elena.gomez@healthybites.com
--   CLIENT         juan.perez@healthybites.com, maria.lopez@healthybites.com, pedro.gomez@healthybites.com
-- =====================================================================================================

-- ---------- identity ----------
insert into users (id, email, password_hash, role, first_name, last_name)
values ('00000000-0000-7000-8000-000000000001', 'admin@healthybites.com',
        '{bcrypt}$2a$10$nfhLRgWbHf02/ur2bqOYjOi3TfQWAOPGs9GsdT72UHcRWrV5nP2ei', 'ADMIN', 'Ana', 'Administradora'),
       ('00000000-0000-7000-8000-000000000002', 'carlos.garcia@healthybites.com',
        '{bcrypt}$2a$10$nfhLRgWbHf02/ur2bqOYjOi3TfQWAOPGs9GsdT72UHcRWrV5nP2ei', 'NUTRITIONIST', 'Carlos', 'García'),
       ('00000000-0000-7000-8000-000000000003', 'elena.gomez@healthybites.com',
        '{bcrypt}$2a$10$nfhLRgWbHf02/ur2bqOYjOi3TfQWAOPGs9GsdT72UHcRWrV5nP2ei', 'NUTRITIONIST', 'Elena', 'Gómez'),
       ('00000000-0000-7000-8000-000000000004', 'juan.perez@healthybites.com',
        '{bcrypt}$2a$10$nfhLRgWbHf02/ur2bqOYjOi3TfQWAOPGs9GsdT72UHcRWrV5nP2ei', 'CLIENT', 'Juan', 'Pérez'),
       ('00000000-0000-7000-8000-000000000005', 'maria.lopez@healthybites.com',
        '{bcrypt}$2a$10$nfhLRgWbHf02/ur2bqOYjOi3TfQWAOPGs9GsdT72UHcRWrV5nP2ei', 'CLIENT', 'María', 'López'),
       ('00000000-0000-7000-8000-000000000006', 'pedro.gomez@healthybites.com',
        '{bcrypt}$2a$10$nfhLRgWbHf02/ur2bqOYjOi3TfQWAOPGs9GsdT72UHcRWrV5nP2ei', 'CLIENT', 'Pedro', 'Gómez')
on conflict do nothing;

-- ---------- profiles ----------
insert into client_profiles (user_id, sex, birth_date, height_cm, weight_kg)
values ('00000000-0000-7000-8000-000000000004', 'MALE', '1998-03-14', 175.0, 72.0),
       ('00000000-0000-7000-8000-000000000005', 'FEMALE', '1995-07-02', 162.0, 60.0),
       ('00000000-0000-7000-8000-000000000006', 'MALE', '1991-11-23', 180.0, 78.0)
on conflict do nothing;

insert into nutritionist_profiles (user_id, bio)
values ('00000000-0000-7000-8000-000000000002',
        'Nutricionista clínico con 10 años de experiencia en control de peso y nutrición deportiva.'),
       ('00000000-0000-7000-8000-000000000003',
        'Especialista en alimentación consciente, planes vegetarianos y salud digestiva.')
on conflict do nothing;

-- ---------- billing ----------
-- María es PREMIUM y Pedro VIP (pagadas, con un año de período para que la demo no caduque). Juan se queda en el
-- plan básico (sin suscripción).
insert into subscriptions (id, user_id, plan_id, tier, status, current_period_start, current_period_end)
select v.id::uuid, v.user_id::uuid, p.id, v.tier, 'ACTIVE', now() - interval '5 days', now() + interval '365 days'
from (values ('00000000-0000-7000-8000-000000000b01', '00000000-0000-7000-8000-000000000005', 'PREMIUM'),
             ('00000000-0000-7000-8000-000000000b02', '00000000-0000-7000-8000-000000000006', 'VIP')) as v(id, user_id, tier)
         join subscription_plans p on p.tier = v.tier
on conflict do nothing;

insert into payments (id, subscription_id, user_id, provider_order_id, provider_capture_id, amount, currency, status,
                      captured_at)
values ('00000000-0000-7000-8000-000000000c01', '00000000-0000-7000-8000-000000000b01',
        '00000000-0000-7000-8000-000000000005', 'SEED-ORDER-MARIA', 'SEED-CAPTURE-MARIA', 5.99, 'USD', 'CAPTURED',
        now() - interval '5 days'),
       ('00000000-0000-7000-8000-000000000c02', '00000000-0000-7000-8000-000000000b02',
        '00000000-0000-7000-8000-000000000006', 'SEED-ORDER-PEDRO', 'SEED-CAPTURE-PEDRO', 9.99, 'USD', 'CAPTURED',
        now() - interval '5 days')
on conflict do nothing;
