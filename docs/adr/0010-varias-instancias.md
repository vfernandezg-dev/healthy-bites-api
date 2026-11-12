# ADR 0010 · Preparada para varias instancias

- **Estado:** aceptada
- **Fecha:** 2026-11-12
- **Sustituye en parte a:** [ADR 0005](0005-eventos-persistidos.md) (el reenvío de eventos al arrancar)

## Contexto

El hosting previsto (contenedores en Vercel con PostgreSQL en Neon) puede levantar varias instancias de la API y
reiniciarlas en cualquier momento. Había estado que solo era correcto con una instancia:

- **Límites de peticiones en memoria:** con N instancias, cada límite se multiplicaba por N.
- **Tareas programadas** (vencimiento de suscripciones, limpiezas, reintento de eventos): corrían en todas las
  instancias a la vez, con conflictos de bloqueo optimista y eventos duplicados.
- **Reenvío al arrancar:** reenviaba todos los eventos pendientes, también los que otra instancia estaba
  procesando.
- **Ficheros en disco local:** ver [ADR 0008](0008-almacenamiento-de-objetos.md).

## Decisión

Todo el estado compartido vive en PostgreSQL, sin servicios nuevos (sin Redis):

- **Límites de peticiones.** `RateLimiter` tiene dos implementaciones, elegidas con `RATE_LIMIT_STORE`.
  - `jdbc` (por defecto) guarda cada evento en `rate_limit_events`.
  - `tryAcquire` serializa cada clave con `pg_advisory_xact_lock`.
  - Las escrituras van en su propia transacción y **no se permiten dentro de otra**: un login fallido cuenta aunque
    se lance el error, y una petición nunca retiene dos conexiones del pool. Por eso login, refresh, registro y
    restablecimiento comprueban los límites antes de abrir su transacción.
  - `memory` queda para una sola instancia.
- **Tareas programadas.** ShedLock con la tabla `shedlock` y la hora de la base de datos: cada tarea corre en una
  sola instancia a la vez. Además son idempotentes, así que una segunda ejecución justo después no repite nada.
- **Eventos.** `republish-outstanding-events-on-restart` pasa a `false`. Las publicaciones atascadas más de 15
  minutos (`spring.modulith.events.staleness.*`) se marcan como fallidas y las reintenta `FailedEventRetryJob`, que
  corre en una sola instancia.
- **Pool de conexiones.** `DB_POOL_SIZE` (10 por defecto) y 5 s de espera: con hilos virtuales el pool es el límite
  real, y sin conexión a tiempo la petición responde 503 `database-unavailable` con `Retry-After`. Instancias ×
  pool tiene que caber en el límite de conexiones de la base de datos.
- **Ninguna espera externa dentro de una transacción.** Las llamadas a PayPal y la escritura de ficheros van entre
  transacciones cortas, sin retener conexiones ni bloqueos.

## Consecuencias

- La API se puede escalar horizontalmente y reiniciar en cualquier momento sin perder límites, ejecuciones de
  tareas ni eventos.
- Cada límite de peticiones cuesta una consulta (o una escritura) en PostgreSQL. Es asumible porque solo se aplica
  a login, refresh, registro, restablecimiento y contacto.
- Un evento atascado tarda hasta unos 30 minutos en reintentarse (15 de staleness y hasta 15 del job), en vez de
  reintentarse en el siguiente arranque.

## Alternativas descartadas

- **Redis para los límites y los bloqueos.** Otro servicio que operar y pagar para un volumen que PostgreSQL
  absorbe sin esfuerzo.
- **Una instancia dedicada a las tareas programadas.** Complica el despliegue y no resuelve los límites ni los
  ficheros.
