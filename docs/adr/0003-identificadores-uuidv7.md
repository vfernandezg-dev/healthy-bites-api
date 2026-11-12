# ADR 0003 · Identificadores UUIDv7

- **Estado:** aceptada
- **Fecha:** 2026-11-11

## Contexto

La versión 1 usaba enteros autoincrementales y tres identificadores por persona (usuario, cliente y
nutricionista). Los ids consecutivos dejaban adivinar recursos ajenos (`/meta/1`, `/meta/2`...) y el registro
llegó a aceptar un id enviado por el cliente, lo que permitía sobrescribir la cuenta del ADMIN (id 1).

## Decisión

- Todas las tablas usan como clave primaria un **UUIDv7** generado en la aplicación
  (`@UuidGenerator(style = VERSION_7)` en `BaseEntity`).
- **Una persona, un id:** `users.id` es también la clave primaria de `client_profiles` y `nutritionist_profiles`.
- Los ids nunca llegan en el cuerpo de una petición para identificar al propietario: salen del token.
- Los datos de demostración usan UUID fijos y legibles (`00000000-0000-7000-8000-000000000004`) para que los tests y
  la documentación puedan referirse a ellos.

## Consecuencias

- Los ids no se pueden adivinar ni enumerar, y se pueden generar antes de guardar (útil para eventos y para
  respuestas `201` con `Location`).
- Al ser ordenables por tiempo, los índices B-tree no se fragmentan como con UUIDv4.
- Ocupan 16 bytes en lugar de 4 u 8: irrelevante para el volumen de datos del producto.

## Alternativas descartadas

- **Enteros autoincrementales.** Enumerables y fuente de los errores de partida.
- **UUIDv4.** Aleatorios: peor localidad en los índices.
- **`uuidv7()` de PostgreSQL 18.** Válido, pero generar el id en la aplicación permite conocerlo antes del
  `INSERT` y no ata el modelo a una versión concreta de la base de datos.
