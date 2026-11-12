# Guía de estilo de la API

Reglas que sigue toda ruta nueva de la API REST de Healthy Bites. La decisión de seguir con REST está en el
[ADR 0009](adr/0009-rest-frente-a-graphql.md). Varias reglas las comprueba la build con `ArchitectureTests`.

## Rutas

- **Base y versión.** Todo cuelga de `/api/v1`. Un cambio incompatible para un cliente que no se despliega a la vez
  que la API abrirá `/api/v2`, o adoptará el versionado nativo de Spring Framework 7.
- **Nombres.** Recursos en plural y en kebab-case: `/meal-plans`, `/content-items`, `/habit-logs`.
- **Agrupación por audiencia:**

  | Prefijo | Audiencia | Protección |
  |---|---|---|
  | `/auth`, `/contact` | Público | `PublicEndpoints` de cada módulo |
  | `/me/**` | El usuario actual | Sesión; `@ClientOnly` si es solo para clientes |
  | `/nutritionist/**` | Espacio del nutricionista | Regla por prefijo en `SecurityConfiguration` |
  | `/admin/**` | Administración | Regla por prefijo en `SecurityConfiguration` |
  | `/meal-plans`, `/content-items`, `/groups`… | Catálogos | Sesión |

- **Recurso propio.** Ningún cuerpo ni ruta lleva el id del propietario: sale del token (`CurrentUser`).
- **Acciones que no son CRUD.** Son `POST /{id}/<verbo>`: `publish`, `archive`, `complete`, `cancel`, `resolve`…
- **Resúmenes para paneles.** Cada módulo resume lo suyo en `GET …/summary` (por ejemplo,
  `/nutritionist/meal-plans/summary`). No hay un módulo agregador.

## Métodos y códigos

| Operación | Respuesta |
|---|---|
| Crear | `201` con `Location` **absoluta**, construida con `Locations.of("/groups/{id}", id)` (incluye `/api/v1`) |
| Leer | `200` |
| Modificar | `PUT` con el recurso completo: `200` con el resultado |
| Acción sin cuerpo de respuesta | `204` |
| Borrar | `204`; idempotente (`204` aunque ya no exista) solo en recursos propios de `/me`, y `404` en el resto |
| Recibido, pero procesado después | `202` |

- **Recurso ajeno.** Responde `404` si es privado (no se revela que existe) y `403` si es visible pero no editable
  (por ejemplo, el post de otro miembro).
- **Conflictos de estado.** Responden `409` con su `code` (`plan-not-draft`, `already-subscribed`…).
- **Errores de validación.** Responden `422` con `errors[{field, message}]`.

## Listados

- **Colecciones que crecen con la plataforma.** Se paginan con `PagedModel`, cuya respuesta es
  `{content, page: {number, size, totalElements, totalPages}}`.
  - Parámetros `?page&size&sort`, con `@ParameterObject` y `@PageableDefault(size = 20, …)`. El máximo es 100.
  - Solo se ordena por campos permitidos, con `Sorting.restrict`.
- **Listas acotadas por el propio usuario.** Las de `/me/*` (metas, rachas, inscripciones…) pueden ser un array.
  Si dependen de fechas, el rango va limitado a 366 días con `DateRanges.check`.
- **Búsqueda de texto.** Es `?q=`: "contiene", sin distinguir mayúsculas, con los comodines escapados
  (`TextSearch`). La columna buscada lleva un índice de trigramas.
- **Filtros por estado.** Son parámetros opcionales: `?status=PUBLISHED`, `?active=true`.

## Cuerpos

- **DTO.** Son `record` de los ficheros `<Módulo>Dtos`, con `of(...)` para construir la respuesta y `toData()`
  para pasar la petición al servicio. Nunca son entidades.
- **Nulabilidad.** Los campos que pueden faltar llevan `@Nullable` (JSpecify): el contrato OpenAPI los marca como
  opcionales y el resto como obligatorios.
- **Validación.** Con Jakarta Validation (`@Valid` en cada `@RequestBody`) y mensajes en español. Las restricciones
  repetidas son anotaciones compuestas (`@EmailAddress`, `@Password`, `@PersonName`, `@WeightKg`).
- **Formatos.** Fechas en ISO-8601 (`Instant` o `LocalDate`, nunca `LocalDateTime`), ids UUID e importes como
  texto (`"5.99"`).

## Errores

- **Formato.** Todo error sale como ProblemDetail ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457)) con un
  `code` estable en kebab-case: `ApiException` o `GlobalExceptionHandler`. El web-app reacciona al `code`, nunca al
  texto.
- **Datos extra.** Van como propiedades del problema: `requiredTier`, `allowed`, `maxDays`…
- **Límites de peticiones.** Responden `429` con `Retry-After`. Si no hay conexión a la base de datos, `503`
  `database-unavailable`, también con `Retry-After`.

## Contrato

- **Exportación.** `OpenApiExportTests` exporta [`openapi.yaml`](openapi.yaml) en cada build, y el CI falla si no
  está en el commit.
- **Web-app.** Regenera sus tipos con `pnpm api:sync && pnpm api:gen`.
- **Cambios incompatibles.** Se marcan con `!` en el commit (`feat(api)!:`) y van en la misma integración que el
  cambio del web-app.

## Deuda conocida

Rutas anteriores a esta guía que no la cumplen. Se mantienen para no romper el contrato:

- `DELETE /nutritionist/rewards/{id}` desactiva la recompensa en lugar de borrarla. Sería `POST /{id}/deactivate`.
- `DELETE /me/streaks/{id}` abandona la racha. Sería `POST /{id}/abandon`.
- `POST /groups/{id}/members` responde `200` con el grupo en lugar de `201`.
- `GET /rewards` es solo para clientes pero no está bajo `/me`.
- `POST /posts/{id}/reports` y `/comments/{id}/reports` responden `202`, aunque la denuncia se guarda al momento.
