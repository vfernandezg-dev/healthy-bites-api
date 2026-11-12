# ADR 0004 · Esquema con Flyway y Hibernate en modo `validate`

- **Estado:** aceptada
- **Fecha:** 2026-11-11

## Contexto

En la versión 1, Hibernate creaba y modificaba las tablas (`ddl-auto=update`) y un `data-test.sql` cargaba los datos
de demostración. No había historial de cambios del esquema, las restricciones dependían de lo que Hibernate dedujera
y no existía una forma segura de actualizar una base de datos con datos reales.

## Decisión

- **Flyway** gestiona el esquema: una migración versionada por módulo (`V2__identity.sql`, `V6__billing.sql`...),
  con claves foráneas, restricciones `CHECK` para los enums, índices únicos parciales (una suscripción activa por
  usuario, una meta activa por cliente) y tipos explícitos (`timestamptz`, `numeric(10,2)`).
- Hibernate solo **valida** que las entidades coinciden con el esquema (`ddl-auto=validate`): si no, la aplicación no
  arranca.
- Una migración ya mergeada **nunca se edita**; cualquier cambio es una migración nueva.
- Los datos de demostración son una migración repetible (`db/seed-dev/R__dev_seed.sql`) que solo se incluye en los
  perfiles `dev` y `test`.

## Consecuencias

- El esquema tiene historial, se revisa en los PR y es el mismo en local, en CI (Testcontainers) y en producción.
- Cada cambio del modelo exige escribir su SQL: más trabajo, pero explícito y revisable.
- Los tests de integración arrancan con las migraciones reales, así que un error de esquema se detecta en CI.

## Alternativas descartadas

- **`ddl-auto=update`.** No borra columnas ni restricciones, no deja historial y puede romper datos reales.
- **Liquibase.** Equivalente; Flyway con SQL plano es más directo para un único motor (PostgreSQL).
