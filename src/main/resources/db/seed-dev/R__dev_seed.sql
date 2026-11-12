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

-- ---------- content ----------
-- Sin portadas ni ficheros: se suben desde la aplicación (nada viene de carpetas locales).
insert into content_items (id, author_id, slug, title, summary, body, type, category, required_tier, status, published_at)
values ('00000000-0000-7000-8000-0000000a0001', '00000000-0000-7000-8000-000000000002', 'guia-de-alimentacion-saludable',
        'Guía de alimentación saludable', 'Principios básicos para armar platos equilibrados en el día a día.',
        'Llena la mitad del plato con verduras, un cuarto con proteína y un cuarto con cereales integrales. Bebe agua a lo largo del día y deja los ultraprocesados para ocasiones especiales.',
        'GUIDE', 'NUTRITION', 'BASIC', 'PUBLISHED', now() - interval '40 days'),
       ('00000000-0000-7000-8000-0000000a0002', '00000000-0000-7000-8000-000000000002',
        'como-interpretar-tu-composicion-corporal', 'Cómo interpretar tu composición corporal',
        'IMC, porcentaje de grasa y masa muscular: qué significan y qué no.',
        'El IMC es una referencia poblacional: no distingue grasa de músculo. Para seguir tu progreso combina el peso con medidas de cintura y cómo te sientes al entrenar.',
        'ARTICLE', 'NUTRITION', 'PREMIUM', 'PUBLISHED', now() - interval '25 days'),
       ('00000000-0000-7000-8000-0000000a0003', '00000000-0000-7000-8000-000000000003', 'rutina-de-fuerza-para-principiantes',
        'Rutina de fuerza para principiantes', 'Los ejercicios básicos y cómo progresar sin lesiones.', null,
        'GUIDE', 'EXERCISE', 'PREMIUM', 'PUBLISHED', now() - interval '20 days'),
       ('00000000-0000-7000-8000-0000000a0004', '00000000-0000-7000-8000-000000000003', 'cardio-de-20-minutos-sin-equipamiento',
        'Cardio de 20 minutos sin equipamiento', 'Una sesión para empezar a moverte en casa.', null,
        'VIDEO', 'EXERCISE', 'BASIC', 'PUBLISHED', now() - interval '18 days'),
       ('00000000-0000-7000-8000-0000000a0005', '00000000-0000-7000-8000-000000000003', 'alimentacion-consciente',
        'Alimentación consciente', 'Técnicas para comer con atención plena y reducir la ansiedad.',
        'Come sin pantallas, mastica despacio y para cuando estés satisfecho, no lleno. Anota cómo te sientes antes y después de comer durante una semana.',
        'ARTICLE', 'MENTAL_HEALTH', 'BASIC', 'PUBLISHED', now() - interval '12 days'),
       ('00000000-0000-7000-8000-0000000a0006', '00000000-0000-7000-8000-000000000002', 'recetas-altas-en-proteina',
        'Recetas altas en proteína', 'Cinco recetas fáciles para ganar masa muscular.', null,
        'VIDEO', 'NUTRITION', 'VIP', 'PUBLISHED', now() - interval '6 days'),
       ('00000000-0000-7000-8000-0000000a0007', '00000000-0000-7000-8000-000000000002', 'meal-prep-de-domingo',
        'Meal prep de domingo (borrador)', 'Cocinar la semana en dos horas.', null,
        'GUIDE', 'NUTRITION', 'PREMIUM', 'DRAFT', null)
on conflict do nothing;

-- Juan desbloqueó la rutina de fuerza con una racha.
insert into content_unlocks (id, content_item_id, user_id, source, unlocked_at)
values ('00000000-0000-7000-8000-0000000a1001', '00000000-0000-7000-8000-0000000a0003',
        '00000000-0000-7000-8000-000000000004', 'REWARD', now() - interval '3 days')
on conflict do nothing;

-- ---------- progress ----------
insert into goals (id, client_id, title, description, target_weight_kg, status, started_on, completed_at)
values ('00000000-0000-7000-8000-000000001001', '00000000-0000-7000-8000-000000000004', 'Bajar a 68 kg',
        'Reducir grasa con un déficit moderado.', 68.0, 'ACTIVE', current_date - 35, null),
       ('00000000-0000-7000-8000-000000001002', '00000000-0000-7000-8000-000000000004', 'Bajar los primeros 2 kg',
        'Primera meta corta.', 74.0, 'COMPLETED', current_date - 70, now() - interval '36 days'),
       ('00000000-0000-7000-8000-000000001003', '00000000-0000-7000-8000-000000000005', 'Mantener 58 kg',
        'Mantener el peso y mejorar la composición corporal.', 58.0, 'ACTIVE', current_date - 35, null),
       ('00000000-0000-7000-8000-000000001004', '00000000-0000-7000-8000-000000000006', 'Llegar a 82 kg',
        'Ganar masa muscular con entrenamiento de fuerza.', 82.0, 'ACTIVE', current_date - 35, null)
on conflict do nothing;

insert into goal_status_changes (id, goal_id, from_status, to_status, changed_at)
values ('00000000-0000-7000-8000-000000001101', '00000000-0000-7000-8000-000000001001', null, 'ACTIVE', now() - interval '35 days'),
       ('00000000-0000-7000-8000-000000001102', '00000000-0000-7000-8000-000000001002', null, 'ACTIVE', now() - interval '70 days'),
       ('00000000-0000-7000-8000-000000001103', '00000000-0000-7000-8000-000000001002', 'ACTIVE', 'COMPLETED', now() - interval '36 days'),
       ('00000000-0000-7000-8000-000000001104', '00000000-0000-7000-8000-000000001003', null, 'ACTIVE', now() - interval '35 days'),
       ('00000000-0000-7000-8000-000000001105', '00000000-0000-7000-8000-000000001004', null, 'ACTIVE', now() - interval '35 days')
on conflict do nothing;

insert into weight_entries (id, goal_id, measured_on, weight_kg, notes)
values ('00000000-0000-7000-8000-000000001201', '00000000-0000-7000-8000-000000001001', current_date - 35, 74.0, 'Inicio del plan'),
       ('00000000-0000-7000-8000-000000001202', '00000000-0000-7000-8000-000000001001', current_date - 28, 73.4, 'Buena adherencia'),
       ('00000000-0000-7000-8000-000000001203', '00000000-0000-7000-8000-000000001001', current_date - 21, 73.0, 'Un par de excesos'),
       ('00000000-0000-7000-8000-000000001204', '00000000-0000-7000-8000-000000001001', current_date - 14, 72.6, 'Empecé a caminar'),
       ('00000000-0000-7000-8000-000000001205', '00000000-0000-7000-8000-000000001001', current_date - 7, 72.3, 'Más energía'),
       ('00000000-0000-7000-8000-000000001206', '00000000-0000-7000-8000-000000001001', current_date - 1, 72.0, 'Sigo bajando'),
       ('00000000-0000-7000-8000-000000001207', '00000000-0000-7000-8000-000000001003', current_date - 28, 60.2, null),
       ('00000000-0000-7000-8000-000000001208', '00000000-0000-7000-8000-000000001003', current_date - 14, 60.1, null),
       ('00000000-0000-7000-8000-000000001209', '00000000-0000-7000-8000-000000001003', current_date - 1, 60.0, 'Manteniendo'),
       ('00000000-0000-7000-8000-00000000120a', '00000000-0000-7000-8000-000000001004', current_date - 21, 77.0, null),
       ('00000000-0000-7000-8000-00000000120b', '00000000-0000-7000-8000-000000001004', current_date - 7, 77.6, null),
       ('00000000-0000-7000-8000-00000000120c', '00000000-0000-7000-8000-000000001004', current_date - 1, 78.0, null)
on conflict do nothing;

insert into habit_logs (id, client_id, logged_on, hydration, nutrition, exercise, sleep, notes)
values ('00000000-0000-7000-8000-000000001301', '00000000-0000-7000-8000-000000000004', current_date - 3, 7, 8, 5, 7, null),
       ('00000000-0000-7000-8000-000000001302', '00000000-0000-7000-8000-000000000004', current_date - 2, 8, 7, 6, 8, null),
       ('00000000-0000-7000-8000-000000001303', '00000000-0000-7000-8000-000000000005', current_date - 2, 6, 8, 7, 9, null),
       ('00000000-0000-7000-8000-000000001304', '00000000-0000-7000-8000-000000000006', current_date - 2, 9, 9, 8, 7, 'Entreno de pierna')
on conflict do nothing;

-- ---------- rewards ----------
insert into rewards (id, author_id, title, description, required_streak_days, content_item_id, meal_plan_id)
values ('00000000-0000-7000-8000-000000002001', '00000000-0000-7000-8000-000000000002', 'Constancia de 7 días',
        'Registra tus hábitos 7 días seguidos y desbloquea el artículo sobre composición corporal.', 7,
        '00000000-0000-7000-8000-0000000a0002', null),
       ('00000000-0000-7000-8000-000000002002', '00000000-0000-7000-8000-000000000003', 'Dos semanas saludables',
        'Mantén tu racha 14 días y accede al plan de masa muscular.', 14, null,
        '00000000-0000-7000-8000-0000000d0002'),
       ('00000000-0000-7000-8000-000000002003', '00000000-0000-7000-8000-000000000002', 'Mes de hábitos',
        'Un mes completo registrando hábitos: recetas altas en proteína.', 30,
        '00000000-0000-7000-8000-0000000a0006', null)
on conflict do nothing;

insert into reward_streaks (id, reward_id, client_id, current_days, last_counted_on, completed_at)
values ('00000000-0000-7000-8000-000000002101', '00000000-0000-7000-8000-000000002001',
        '00000000-0000-7000-8000-000000000004', 2, current_date - 2, null),
       ('00000000-0000-7000-8000-000000002102', '00000000-0000-7000-8000-000000002002',
        '00000000-0000-7000-8000-000000000005', 10, current_date - 2, null),
       ('00000000-0000-7000-8000-000000002103', '00000000-0000-7000-8000-000000002003',
        '00000000-0000-7000-8000-000000000006', 2, current_date - 2, null)
on conflict do nothing;
