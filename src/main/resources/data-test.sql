INSERT INTO roles (nombre)
VALUES
    ('ADMIN'),
    ('CLIENTE'),
    ('NUTRICIONISTA')
    ON CONFLICT DO NOTHING;

INSERT INTO public.cliente (nombre, apellido, sexo, edad, altura, peso, fecha_creacion, fecha_actualizacion)
VALUES
    ('Juan', 'Perez', 'M', 25, 1.75, 70, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Maria', 'Lopez', 'F', 30, 1.60, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Pedro', 'Gomez', 'M', 35, 1.80, 80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Ana', 'Martinez', 'F', 40, 1.70, 65, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT DO NOTHING;

INSERT INTO public.nutricionista (nombre, apellido, bio, fecha_creacion, fecha_actualizacion)
VALUES
    ('Carlos', 'García', 'Esta es la bio del nutricionista 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Luis', 'Martínez', 'Esta es la bio del nutricionista 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Elena', 'Gómez', 'Esta es la bio del nutricionista 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Pedro', 'Alonso', 'Esta es la bio del nutricionista 4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT DO NOTHING;

INSERT INTO public.contenido (titulo, descripcion, tipo_contenido, categoria_contenido, es_gratis, fecha_creacion, fecha_actualizacion, id_nutricionista)
VALUES
    ('Dieta para bajar de peso', 'Esta dieta ayuda a bajar de peso', 'VIDEO', 'NUTRICION', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('Dieta para subir de peso', 'Esta dieta ayuda a subir de peso', 'ARTICULO', 'EJERCICIO', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    ('Dieta para mantener peso', 'Esta dieta ayuda a mantener peso', 'GUIA', 'EJERCICIO', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
    ('Ejercicios para bajar de peso', 'Estos ejercicios son para bajar de peso', 'ARTICULO', 'SALUD_MENTAL', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4)
    ON CONFLICT DO NOTHING;

INSERT INTO public.plan_alimenticio (plan_objetivo, descripcion, duracion_dias, es_gratis, fecha_creacion, fecha_actualizacion, id_nutricionista)
VALUES
    ('DEFICIT', 'Plan bajo en carbohidratos con alto contenido en proteínas', 30, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
    ('AUMENTO_MASA_MUSCULAR', 'Dieta rica en proteínas y calorías para el aumento de masa muscular', 60, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
    ('MANTENIMIENTO_PESO', 'Plan balanceado para mantener el peso actual', 45, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3),
    ('DEFICIT', 'Plan de desintoxicación de 7 días basado en jugos y alimentos naturales', 7, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 4)
    ON CONFLICT DO NOTHING;

INSERT INTO public.grupos (nombre, es_privado, fecha_creacion, fecha_actualizacion)
VALUES
    ('Grupo de pérdida de peso', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Grupo de corredores', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Grupo de alimentación saludable', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT DO NOTHING;


