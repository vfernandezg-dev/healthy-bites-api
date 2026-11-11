-- =====================================================================================
-- Healthy Bites - datos de prueba (demo)
--
-- Se ejecuta en cada arranque (spring.sql.init.mode=always) DESPUES de que Hibernate cree
-- las tablas (spring.jpa.defer-datasource-initialization=true).
-- Es idempotente: IDs explicitos + ON CONFLICT DO NOTHING, y al final se reajustan las
-- secuencias IDENTITY para que los inserts hechos desde la app no choquen con estos IDs.
-- Para arrancar sin inyectar datos: DB_SEED_MODE=never
--
-- Credenciales demo (contrasena para todos: healthy123)
--   ADMIN          admin@healthybites.com
--   NUTRICIONISTA  carlos.garcia@healthybites.com, elena.gomez@healthybites.com
--   CLIENTE        juan.perez@healthybites.com (BASICO), maria.lopez@healthybites.com (PREMIUM),
--                  pedro.gomez@healthybites.com (VIP)
-- =====================================================================================

-- ---------- Roles ----------
INSERT INTO roles (nombre)
VALUES ('ADMIN'), ('CLIENTE'), ('NUTRICIONISTA')
ON CONFLICT DO NOTHING;

-- ---------- Usuarios (hash BCrypt de 'healthy123') ----------
INSERT INTO usuario (id, correo, contrasena, id_rol)
SELECT v.id, v.correo, '$2a$10$nfhLRgWbHf02/ur2bqOYjOi3TfQWAOPGs9GsdT72UHcRWrV5nP2ei', r.id
FROM (VALUES
          (1, 'admin@healthybites.com',         'ADMIN'),
          (2, 'carlos.garcia@healthybites.com', 'NUTRICIONISTA'),
          (3, 'elena.gomez@healthybites.com',   'NUTRICIONISTA'),
          (4, 'juan.perez@healthybites.com',    'CLIENTE'),
          (5, 'maria.lopez@healthybites.com',   'CLIENTE'),
          (6, 'pedro.gomez@healthybites.com',   'CLIENTE')
     ) AS v(id, correo, rol)
JOIN roles r ON r.nombre = v.rol
ON CONFLICT DO NOTHING;

-- ---------- Perfiles ----------
INSERT INTO nutricionista (id, nombre, apellido, bio, fecha_creacion, fecha_actualizacion, id_usuario)
VALUES
    (1, 'Carlos', 'García', 'Nutricionista clínico con 10 años de experiencia en control de peso y nutrición deportiva.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (2, 'Elena', 'Gómez', 'Especialista en alimentación consciente, planes vegetarianos y salud digestiva.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3)
ON CONFLICT DO NOTHING;

INSERT INTO cliente (id, nombre, apellido, sexo, edad, altura, peso, fecha_creacion, fecha_actualizacion, id_usuario)
VALUES
    (1, 'Juan',  'Pérez', 'Masculino', 28, 1.75, 72.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4),
    (2, 'María', 'López', 'Femenino',  31, 1.62, 60.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 5),
    (3, 'Pedro', 'Gómez', 'Masculino', 35, 1.80, 78.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 6)
ON CONFLICT DO NOTHING;

-- ---------- Suscripciones ----------
INSERT INTO suscripcion (id, tipo_suscripcion, precio, estado_pago, fecha_creacion, id_usuario)
VALUES
    (1, 'BASICO',  0.00, 'PAGADO', CURRENT_TIMESTAMP, 4),
    (2, 'PREMIUM', 5.99, 'PAGADO', CURRENT_TIMESTAMP, 5),
    (3, 'VIP',     9.99, 'PAGADO', CURRENT_TIMESTAMP, 6)
ON CONFLICT DO NOTHING;

-- ---------- Contenidos (sin portada: mediafiles/ no se versiona; se suben desde la app) ----------
INSERT INTO contenido (id, titulo, descripcion, slug, tipo_contenido, categoria_contenido, cover_path, file_path, es_gratis, fecha_creacion, fecha_actualizacion, id_nutricionista)
VALUES
    (1, 'Guía de alimentación saludable', 'Principios básicos para armar platos equilibrados en el día a día.', 'guia-alimentacion-saludable', 'GUIA', 'NUTRICION', NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (2, 'Valoración nutricional y composición corporal', 'Cómo interpretar tu IMC, porcentaje de grasa y masa muscular.', 'valoracion-nutricional-composicion-corporal', 'ARTICULO', 'NUTRICION', NULL, NULL, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (3, 'Anatomía del ejercicio: rutina de fuerza', 'Guía ilustrada de los principales músculos y ejercicios de fuerza.', 'anatomia-ejercicio-rutina-fuerza', 'GUIA', 'EJERCICIO', NULL, NULL, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (4, 'Rutina de cardio para principiantes', 'Sesión de 20 minutos para empezar a moverte sin equipamiento.', 'rutina-cardio-principiantes', 'VIDEO', 'EJERCICIO', NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (5, 'Mindfulness y alimentación consciente', 'Técnicas para comer con atención plena y reducir la ansiedad.', 'mindfulness-alimentacion-consciente', 'ARTICULO', 'SALUD_MENTAL', NULL, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (6, 'Recetas altas en proteína', 'Cinco recetas fáciles para apoyar el aumento de masa muscular.', 'recetas-altas-proteina', 'VIDEO', 'NUTRICION', NULL, NULL, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1)
ON CONFLICT DO NOTHING;

-- ---------- Planes alimenticios y comidas ----------
INSERT INTO plan_alimenticio (id, plan_objetivo, descripcion, duracion_dias, es_gratis, fecha_creacion, fecha_actualizacion, id_nutricionista)
VALUES
    (1, 'DEFICIT', 'Plan bajo en carbohidratos con alto contenido en proteínas', 30, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (2, 'AUMENTO_MASA_MUSCULAR', 'Dieta rica en proteínas y calorías para el aumento de masa muscular', 60, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (3, 'MANTENIMIENTO_PESO', 'Plan balanceado para mantener el peso actual', 45, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (4, 'DEFICIT', 'Plan de desintoxicación de 7 días basado en jugos y alimentos naturales', 7, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2)
ON CONFLICT DO NOTHING;

INSERT INTO comida_diaria (id, nombre_comida, calorias, categoria, fecha_creacion, fecha_actualizacion, id_planalimenticio)
VALUES
    (1,  'Omelette de claras con espinaca', 250, 'DESAYUNO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (2,  'Pechuga a la plancha con ensalada', 420, 'ALMUERZO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (3,  'Yogur griego con nueces', 180, 'SNACK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (4,  'Salmón al horno con brócoli', 380, 'CENA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (5,  'Avena con plátano y mantequilla de maní', 550, 'DESAYUNO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (6,  'Arroz integral con carne y frejoles', 850, 'ALMUERZO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (7,  'Batido de proteína con leche', 350, 'SNACK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (8,  'Pasta integral con pollo', 700, 'CENA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (9,  'Tostadas integrales con palta', 320, 'DESAYUNO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
    (10, 'Quinua con verduras salteadas', 480, 'ALMUERZO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
    (11, 'Sopa de verduras con huevo', 300, 'CENA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
    (12, 'Jugo verde de manzana y apio', 150, 'DESAYUNO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4),
    (13, 'Ensalada de frutas', 200, 'SNACK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4),
    (14, 'Crema de zapallo', 220, 'CENA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4)
ON CONFLICT DO NOTHING;

-- ---------- Recompensas ----------
INSERT INTO recompensa (id, nombre, descripcion, dias_requeridos, fecha_creacion, fecha_actualizacion, id_nutricionista, contenido_id, plan_alimenticio_id)
VALUES
    (1, 'Constancia de 7 días', 'Registra tus hábitos 7 días seguidos y desbloquea una guía de fuerza.', 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 3, NULL),
    (2, 'Dos semanas saludables', 'Mantén tu racha 14 días y accede al plan de masa muscular.', 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, NULL, 2),
    (3, 'Mes de hábitos', 'Un mes completo registrando hábitos: recetas altas en proteína.', 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 6, NULL)
ON CONFLICT DO NOTHING;

-- ---------- Comunidad: grupos, membresías, publicaciones y comentarios ----------
INSERT INTO grupos (id, nombre, cantidad_miembros, es_privado, fecha_creacion, fecha_actualizacion, id_cliente)
VALUES
    (1, 'Grupo de pérdida de peso', 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (2, 'Grupo de corredores', 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (3, 'Grupo de alimentación saludable', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3)
ON CONFLICT DO NOTHING;

INSERT INTO unirse (cliente_id, grupo_id, fecha_creacion)
VALUES
    (2, 1, CURRENT_TIMESTAMP),
    (1, 2, CURRENT_TIMESTAMP),
    (3, 2, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

INSERT INTO publicacion (id, titulo, descripcion, publicacion_path, fecha_creacion, fecha_actualizacion, id_cliente, id_grupo)
VALUES
    (1, 'Mi primera semana', 'Bajé 1 kg cambiando las bebidas azucaradas por agua.', NULL, CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '6 days', 1, 1),
    (2, 'Corrí mis primeros 5K', 'Después de un mes de entrenamiento logré completar 5 km sin parar.', NULL, CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '4 days', 2, 2),
    (3, 'Rutina de los sábados', '¿Alguien se anima a trotar el sábado temprano en el parque?', NULL, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 3, 2),
    (4, 'Receta: ensalada de quinua', 'Quinua, tomate, pepino, palta y limón. Rápida y muy saciante.', NULL, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 2, 1)
ON CONFLICT DO NOTHING;

INSERT INTO comentario (id, mensaje, fecha_creacion, fecha_actualizacion, id_cliente, id_publicacion)
VALUES
    (1, '¡Excelente inicio, Juan! Sigue así.', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 2, 1),
    (2, 'Felicitaciones, gran logro.', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 1, 2),
    (3, 'Yo me apunto, ¿a qué hora?', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 1, 3),
    (4, 'La probé y quedó buenísima.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 4),
    (5, 'Gracias por compartir la receta.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 4)
ON CONFLICT DO NOTHING;

-- ---------- Metas, historial de estados y seguimiento de peso ----------
INSERT INTO meta (id, nombre, descripcion, peso_objetivo, estado, fecha_creacion, fecha_actualizacion, id_cliente)
VALUES
    (1, 'Bajar a 68 kg', 'Reducir grasa corporal con déficit calórico moderado.', 68.0, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '35 days', CURRENT_TIMESTAMP, 1),
    (2, 'Mantener 58 kg', 'Mantener el peso y mejorar la composición corporal.', 58.0, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '35 days', CURRENT_TIMESTAMP, 2),
    (3, 'Llegar a 82 kg de masa magra', 'Ganar masa muscular con entrenamiento de fuerza.', 82.0, 'ACTIVE', CURRENT_TIMESTAMP - INTERVAL '35 days', CURRENT_TIMESTAMP, 3),
    (4, 'Bajar los primeros 2 kg', 'Primera meta corta (ya cumplida).', 74.0, 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '70 days', CURRENT_TIMESTAMP - INTERVAL '36 days', 1)
ON CONFLICT DO NOTHING;

INSERT INTO meta_historial_estado (id_meta, orden, estado)
VALUES
    (1, 0, 'ACTIVE'),
    (2, 0, 'ACTIVE'),
    (3, 0, 'ACTIVE'),
    (4, 0, 'ACTIVE'),
    (4, 1, 'COMPLETED')
ON CONFLICT DO NOTHING;

INSERT INTO seguimiento (id, fecha, peso_del_dia, obsevaciones, fecha_creacion, fecha_actualizacion, id_meta)
VALUES
    (1,  CURRENT_TIMESTAMP - INTERVAL '35 days', 74.0, 'Inicio del plan', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (2,  CURRENT_TIMESTAMP - INTERVAL '28 days', 73.4, 'Buena adherencia a la dieta', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (3,  CURRENT_TIMESTAMP - INTERVAL '21 days', 73.0, 'Semana con un par de excesos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (4,  CURRENT_TIMESTAMP - INTERVAL '14 days', 72.6, 'Empecé a caminar 30 minutos diarios', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (5,  CURRENT_TIMESTAMP - INTERVAL '7 days',  72.3, 'Me siento con más energía', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (6,  CURRENT_TIMESTAMP - INTERVAL '1 day',   72.0, 'Sigo bajando de a poco', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    (7,  CURRENT_TIMESTAMP - INTERVAL '35 days', 60.5, 'Peso inicial', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (8,  CURRENT_TIMESTAMP - INTERVAL '28 days', 60.2, 'Semana estable', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (9,  CURRENT_TIMESTAMP - INTERVAL '21 days', 59.8, 'Más cardio esta semana', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (10, CURRENT_TIMESTAMP - INTERVAL '14 days', 60.1, 'Retención de líquidos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (11, CURRENT_TIMESTAMP - INTERVAL '7 days',  59.9, 'Todo en orden', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (12, CURRENT_TIMESTAMP - INTERVAL '1 day',   60.0, 'Manteniendo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    (13, CURRENT_TIMESTAMP - INTERVAL '35 days', 76.0, 'Inicio del volumen', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
    (14, CURRENT_TIMESTAMP - INTERVAL '28 days', 76.5, 'Aumenté las porciones de proteína', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
    (15, CURRENT_TIMESTAMP - INTERVAL '21 days', 77.0, 'Nuevo récord en sentadilla', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
    (16, CURRENT_TIMESTAMP - INTERVAL '14 days', 77.2, 'Semana de descarga', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
    (17, CURRENT_TIMESTAMP - INTERVAL '7 days',  77.6, 'Buen progreso', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
    (18, CURRENT_TIMESTAMP - INTERVAL '1 day',   78.0, 'Objetivo cada vez más cerca', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3)
ON CONFLICT DO NOTHING;

-- ---------- Hábitos diarios (hidratación en L, alimentación en kcal, ejercicio y sueño en horas) ----------
INSERT INTO habito (id, nombre, fecha_registro, hidratacion, alimentacion, ejercicio, calidad_de_sueno, fecha_actualizacion, id_cliente)
VALUES
    (1, 'Registro diario', CURRENT_TIMESTAMP - INTERVAL '3 days', 2.0, 1900, 0.5, 7.0, CURRENT_TIMESTAMP, 1),
    (2, 'Registro diario', CURRENT_TIMESTAMP - INTERVAL '2 days', 2.5, 1850, 1.0, 7.5, CURRENT_TIMESTAMP, 1),
    (3, 'Registro diario', CURRENT_TIMESTAMP - INTERVAL '1 day',  2.2, 1800, 0.5, 6.5, CURRENT_TIMESTAMP, 1),
    (4, 'Registro diario', CURRENT_TIMESTAMP - INTERVAL '2 days', 1.8, 1700, 1.0, 8.0, CURRENT_TIMESTAMP, 2),
    (5, 'Registro diario', CURRENT_TIMESTAMP - INTERVAL '1 day',  2.0, 1750, 1.5, 7.0, CURRENT_TIMESTAMP, 2),
    (6, 'Registro diario', CURRENT_TIMESTAMP - INTERVAL '2 days', 3.0, 2800, 1.5, 7.0, CURRENT_TIMESTAMP, 3),
    (7, 'Registro diario', CURRENT_TIMESTAMP - INTERVAL '1 day',  3.2, 2900, 2.0, 7.5, CURRENT_TIMESTAMP, 3)
ON CONFLICT DO NOTHING;

-- ---------- Rachas (cliente <-> recompensa) ----------
INSERT INTO racha (cliente_id, recompensa_id, contador_dias, entregada, ultima_fecha_registro)
VALUES
    (1, 1, 3,  false, CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (2, 1, 7,  true,  CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (2, 2, 10, false, CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (3, 3, 2,  false, CURRENT_TIMESTAMP - INTERVAL '1 day')
ON CONFLICT DO NOTHING;

-- ---------- Accesos a contenidos y planes ----------
INSERT INTO acceso_contenido (cliente_id, contenido_id, fecha_creacion)
VALUES
    (2, 2, CURRENT_TIMESTAMP),
    (2, 3, CURRENT_TIMESTAMP),
    (3, 1, CURRENT_TIMESTAMP),
    (3, 3, CURRENT_TIMESTAMP),
    (3, 6, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

INSERT INTO acceso_plan (cliente_id, plan_alimenticio_id, fecha_creacion)
VALUES
    (1, 3, CURRENT_TIMESTAMP),
    (2, 1, CURRENT_TIMESTAMP),
    (3, 2, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ---------- Reajuste de secuencias IDENTITY (siguiente id = max + 1) ----------
SELECT setval(pg_get_serial_sequence('roles', 'id'),            (SELECT COALESCE(MAX(id), 0) + 1 FROM roles), false);
SELECT setval(pg_get_serial_sequence('usuario', 'id'),          (SELECT COALESCE(MAX(id), 0) + 1 FROM usuario), false);
SELECT setval(pg_get_serial_sequence('nutricionista', 'id'),    (SELECT COALESCE(MAX(id), 0) + 1 FROM nutricionista), false);
SELECT setval(pg_get_serial_sequence('cliente', 'id'),          (SELECT COALESCE(MAX(id), 0) + 1 FROM cliente), false);
SELECT setval(pg_get_serial_sequence('suscripcion', 'id'),      (SELECT COALESCE(MAX(id), 0) + 1 FROM suscripcion), false);
SELECT setval(pg_get_serial_sequence('contenido', 'id'),        (SELECT COALESCE(MAX(id), 0) + 1 FROM contenido), false);
SELECT setval(pg_get_serial_sequence('plan_alimenticio', 'id'), (SELECT COALESCE(MAX(id), 0) + 1 FROM plan_alimenticio), false);
SELECT setval(pg_get_serial_sequence('comida_diaria', 'id'),    (SELECT COALESCE(MAX(id), 0) + 1 FROM comida_diaria), false);
SELECT setval(pg_get_serial_sequence('recompensa', 'id'),       (SELECT COALESCE(MAX(id), 0) + 1 FROM recompensa), false);
SELECT setval(pg_get_serial_sequence('grupos', 'id'),           (SELECT COALESCE(MAX(id), 0) + 1 FROM grupos), false);
SELECT setval(pg_get_serial_sequence('publicacion', 'id'),      (SELECT COALESCE(MAX(id), 0) + 1 FROM publicacion), false);
SELECT setval(pg_get_serial_sequence('comentario', 'id'),       (SELECT COALESCE(MAX(id), 0) + 1 FROM comentario), false);
SELECT setval(pg_get_serial_sequence('meta', 'id'),             (SELECT COALESCE(MAX(id), 0) + 1 FROM meta), false);
SELECT setval(pg_get_serial_sequence('seguimiento', 'id'),      (SELECT COALESCE(MAX(id), 0) + 1 FROM seguimiento), false);
SELECT setval(pg_get_serial_sequence('habito', 'id'),           (SELECT COALESCE(MAX(id), 0) + 1 FROM habito), false);
