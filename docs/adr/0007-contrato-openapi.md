# ADR 0007 · Contrato OpenAPI versionado y tipos generados en el web-app

- **Estado:** aceptada
- **Fecha:** 2026-11-11

## Contexto

En la versión 1, el web-app declaraba a mano sus interfaces TypeScript y a menudo no coincidían con la API (ids
cruzados, fechas como texto libre, campos que ya no existían). Los fallos aparecían en el navegador, no en el build.

## Decisión

- `OpenApiExportTests` exporta el contrato a [`docs/openapi.yaml`](../openapi.yaml) en cada build, con salida
  estable (claves ordenadas). CI falla si el fichero difiere del commiteado: todo cambio de la API aparece en el
  diff del PR.
- El web-app copia ese fichero (`pnpm api:sync`, anotando el commit de origen) y genera sus tipos con
  `openapi-typescript` (`pnpm api:gen`). Su CI comprueba que los tipos generados están al día (`pnpm api:check`).
- Los DTO de la API son `record` con anotaciones de validación y de nulabilidad (JSpecify), así que el contrato
  refleja qué campos son obligatorios.

## Consecuencias

- Un cambio incompatible en la API rompe el build del web-app en lugar de fallar en producción.
- Swagger UI solo se publica en desarrollo; el contrato sigue disponible en el repositorio para cualquiera.
- Cambiar la API exige regenerar y commitear el contrato: un paso más, pero visible en la revisión.

## Alternativas descartadas

- **Tipos escritos a mano.** Es el problema de partida.
- **Generar un cliente HTTP completo.** Añade código generado difícil de revisar; los tipos bastan, porque el
  web-app usa `HttpClient` y `httpResource` de Angular.
