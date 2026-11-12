# ADR 0009 · REST estandarizado en lugar de GraphQL

- **Estado:** aceptada
- **Fecha:** 2026-11-12

## Contexto

Antes de que la API crezca se evaluó si convenía migrar a GraphQL (Spring for GraphQL) o adoptar un enfoque
híbrido. Datos medidos en noviembre de 2026:

- **Consumidores:** un único SPA (el web-app, del mismo equipo) y la landing, que solo llama a `POST /contact`. No
  hay app móvil ni terceros.
- **Uso:** el web-app usa unas 101 de las 104 operaciones de la API.
- **Llamadas por pantalla:** como mucho 5 al cargar (el panel del cliente), con cascadas de profundidad 2.
- **Datos de más:** el único caso real era el panel del nutricionista, que descargaba 100 planes y 100 contenidos
  para contarlos. Se debía a que faltaban dos endpoints, no al modelo REST.
- **Contrato:** ya hay un flujo OpenAPI → tipos TypeScript con control de deriva en el CI de los dos repositorios
  ([ADR 0007](0007-contrato-openapi.md)).

## Decisión

**Mantener REST y estandarizarlo** con la guía [`docs/api-guidelines.md`](../api-guidelines.md):

- paginación con `PagedModel` en toda colección que crece;
- `201` con una `Location` absoluta;
- endpoints `/summary` por módulo para los paneles;
- errores RFC 9457;
- versionado por ruta (`/api/v1`).

El versionado nativo de Spring Framework 7 (`ApiVersionConfigurer`) solo se adoptará cuando haga falta un cambio
incompatible con clientes que no se despliegan a la vez.

### Cuándo reconsiderarlo

Se pasaría a un enfoque híbrido (GraphQL de solo lectura delante de los servicios `app`, y REST para sesión,
ficheros, pagos y webhooks) si:

- aparece un segundo cliente con necesidades distintas (app móvil, API para partners);
- las pantallas superan las 5 llamadas o las cascadas de profundidad 2;
- el frontend necesita componer vistas nuevas sin cambios en el backend.

## Consecuencias

- Coste bajo: los dos endpoints que faltaban costaron días; GraphQL habría costado semanas. Ese coste incluye el
  esquema de unos 30 tipos, los resolvers, la autorización por campo y reescribir los 13 servicios de datos del
  web-app.
- Se conserva lo que REST da gratis:
  - caché HTTP (ficheros `immutable`, `no-store` en lo sensible);
  - códigos de estado que el web-app ya interpreta (401 con refresh, 429 con `Retry-After`, 403 con
    `requiredTier`);
  - subidas multipart, `Range` y redirecciones (PayPal, URLs prefirmadas).
- Cada módulo sigue exponiendo sus rutas. Un esquema GraphQL sería transversal: un módulo dependería de todos los
  demás o el esquema quedaría fragmentado.

## Alternativas descartadas

- **GraphQL completo.** Habría que mantener REST igualmente para la sesión con cookies, las subidas, las descargas
  firmadas y PayPal. Añade N+1 en los resolvers (cada campo entre módulos necesita un DataLoader), límites de
  profundidad y coste, y errores dentro de respuestas 200.
- **Híbrido ahora.** Dos modelos de contrato, de error y de seguridad que mantener, sin un cliente que lo necesite.
