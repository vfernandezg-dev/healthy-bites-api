INSERT INTO public.cliente (nombre, apellido, sexo, edad, altura, peso)
VALUES
    ('Juan', 'Perez', 'M', 25, 1.75, 70),
    ('Maria', 'Lopez', 'F', 30, 1.60, 60),
    ('Pedro', 'Gomez', 'M', 35, 1.80, 80),
    ('Ana', 'Martinez', 'F', 40, 1.70, 65)
    ON CONFLICT DO NOTHING;

INSERT INTO public.nutricionista (nombre, apellido)
VALUES
    ('Carlos', 'García'),
    ('Luis', 'Martínez'),
    ('Elena', 'Gómez'),
    ('Pedro', 'Alonso')
    ON CONFLICT DO NOTHING;

INSERT INTO public.contenido (titulo, descripcion, tipo_contenido, categoria_contenido, es_gratis, id_nutricionista)
VALUES
    ('Dieta para bajar de peso', 'Esta dieta ayuda a bajar de peso', 'VIDEO', 'NUTRICION', true, 1),
    ('Dieta para subir de peso', 'Esta dieta ayuda a subir de peso', 'ARTICULO', 'EJERCICIO', true, 2),
    ('Dieta para mantener peso', 'Esta dieta ayuda a mantener peso', 'GUIA', 'EJERCICIO', false, 3),
    ('Ejercicios para bajar de peso', 'Estos ejercicios son para bajar de peso', 'ARTICULO', 'SALUD_MENTAL', true, 4)
    ON CONFLICT DO NOTHING;

INSERT INTO public.plan_alimenticio (plan_objetivo, descripcion, duracion_dias, es_gratis, id_nutricionista)
VALUES
    ('DEFICIT', 'Plan bajo en carbohidratos con alto contenido en proteínas', 30, false, 1),
    ('AUMENTO_MASA_MUSCULAR', 'Dieta rica en proteínas y calorías para el aumento de masa muscular', 60, false, 2),
    ('MANTENIMIENTO_PESO', 'Plan balanceado para mantener el peso actual', 45, true, 3),
    ('DEFICIT', 'Plan de desintoxicación de 7 días basado en jugos y alimentos naturales', 7, true, 4)
    ON CONFLICT DO NOTHING;

INSERT INTO public.grupos (nombre, es_privado)
VALUES
    ('Grupo de pérdida de peso', true),
    ('Grupo de corredores', false),
    ('Grupo de alimentación saludable', true)
    ON CONFLICT DO NOTHING;


