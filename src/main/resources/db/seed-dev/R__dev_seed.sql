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

-- ---------- nutrition ----------
insert into meal_plans (id, author_id, title, description, goal, duration_days, required_tier, status, published_at)
values ('00000000-0000-7000-8000-0000000d0001', '00000000-0000-7000-8000-000000000002', 'Déficit suave en 4 semanas',
        'Plan bajo en azúcares y alto en proteína para perder grasa sin pasar hambre.', 'WEIGHT_LOSS', 28, 'PREMIUM',
        'PUBLISHED', now() - interval '20 days'),
       ('00000000-0000-7000-8000-0000000d0002', '00000000-0000-7000-8000-000000000003', 'Masa muscular en 8 semanas',
        'Dieta rica en proteínas y calorías de calidad para acompañar el entrenamiento de fuerza.', 'MUSCLE_GAIN', 56,
        'VIP', 'PUBLISHED', now() - interval '15 days'),
       ('00000000-0000-7000-8000-0000000d0003', '00000000-0000-7000-8000-000000000002', 'Mantenimiento equilibrado',
        'Comidas sencillas y variadas para mantener el peso con energía todo el día.', 'MAINTENANCE', 14, 'BASIC',
        'PUBLISHED', now() - interval '30 days'),
       ('00000000-0000-7000-8000-0000000d0004', '00000000-0000-7000-8000-000000000003', 'Semana ligera',
        'Siete días de platos ligeros con fruta, verdura y legumbres para empezar con buen pie.', 'WEIGHT_LOSS', 7,
        'BASIC', 'PUBLISHED', now() - interval '10 days'),
       ('00000000-0000-7000-8000-0000000d0005', '00000000-0000-7000-8000-000000000002', 'Plan vegetariano (borrador)',
        'Propuesta vegetariana en preparación.', 'MAINTENANCE', 14, 'PREMIUM', 'DRAFT', null)
on conflict do nothing;

insert into meals (id, meal_plan_id, day_number, position, category, name, description, calories)
values ('00000000-0000-7000-8000-0000000e0001', '00000000-0000-7000-8000-0000000d0001', 1, 0, 'BREAKFAST',
        'Tortilla de claras con espinaca', 'Tres claras, espinaca y una tostada integral.', 250),
       ('00000000-0000-7000-8000-0000000e0002', '00000000-0000-7000-8000-0000000d0001', 1, 1, 'LUNCH',
        'Pechuga a la plancha con ensalada', null, 420),
       ('00000000-0000-7000-8000-0000000e0003', '00000000-0000-7000-8000-0000000d0001', 1, 2, 'DINNER',
        'Salmón al horno con brócoli', null, 380),
       ('00000000-0000-7000-8000-0000000e0004', '00000000-0000-7000-8000-0000000d0001', 2, 0, 'BREAKFAST',
        'Yogur griego con nueces', null, 230),
       ('00000000-0000-7000-8000-0000000e0005', '00000000-0000-7000-8000-0000000d0002', 1, 0, 'BREAKFAST',
        'Avena con plátano y mantequilla de maní', null, 550),
       ('00000000-0000-7000-8000-0000000e0006', '00000000-0000-7000-8000-0000000d0002', 1, 1, 'LUNCH',
        'Arroz integral con carne y frijoles', null, 850),
       ('00000000-0000-7000-8000-0000000e0007', '00000000-0000-7000-8000-0000000d0003', 1, 0, 'BREAKFAST',
        'Tostadas integrales con palta', null, 320),
       ('00000000-0000-7000-8000-0000000e0008', '00000000-0000-7000-8000-0000000d0003', 1, 1, 'LUNCH',
        'Quinua con verduras salteadas', null, 480),
       ('00000000-0000-7000-8000-0000000e0009', '00000000-0000-7000-8000-0000000d0004', 1, 0, 'BREAKFAST',
        'Batido verde de manzana y apio', null, 150),
       ('00000000-0000-7000-8000-0000000e000a', '00000000-0000-7000-8000-0000000d0004', 1, 1, 'DINNER',
        'Crema de zapallo', null, 220)
on conflict do nothing;

insert into meal_plan_enrollments (id, meal_plan_id, client_id, source, enrolled_at)
values ('00000000-0000-7000-8000-0000000f0001', '00000000-0000-7000-8000-0000000d0003',
        '00000000-0000-7000-8000-000000000004', 'SELF', now() - interval '12 days'),
       ('00000000-0000-7000-8000-0000000f0002', '00000000-0000-7000-8000-0000000d0001',
        '00000000-0000-7000-8000-000000000005', 'SELF', now() - interval '8 days'),
       ('00000000-0000-7000-8000-0000000f0003', '00000000-0000-7000-8000-0000000d0002',
        '00000000-0000-7000-8000-000000000006', 'SELF', now() - interval '6 days')
on conflict do nothing;
