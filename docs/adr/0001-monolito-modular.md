# ADR 0001 · Monolito modular con Spring Modulith

- **Estado:** aceptada
- **Fecha:** 2026-11-11

## Contexto

La versión 1 seguía una arquitectura en capas técnicas (`api`, `service`, `repository`, `model`...). Cada
funcionalidad estaba repartida por todos los paquetes, las entidades se referenciaban entre sí sin límite (un
`Cliente` llegaba a casi todo el modelo) y había once controladores `Admin*` casi idénticos. Cambiar una
funcionalidad obligaba a tocar medio proyecto y nada impedía que una dependencia nueva creara un ciclo.

El equipo es pequeño y el tráfico, modesto: separar en microservicios añadiría despliegues, red y consistencia
eventual sin ningún beneficio real.

## Decisión

Un único despliegue organizado en **módulos por funcionalidad** (`identity`, `profiles`, `billing`, `nutrition`,
`content`, `media`, `progress`, `rewards`, `community`, `notifications`, `contact`) más un núcleo abierto, `shared`.

- Cada módulo publica su API en el paquete raíz (interfaces y `record`) y sus eventos en `events`
  (`@NamedInterface`). Los paquetes `domain`, `app`, `web` e `infra` son internos.
- Entre módulos solo se comparten identificadores: ninguna entidad tiene un `@ManyToOne` hacia la de otro módulo.
  La integridad la siguen garantizando las claves foráneas de la base de datos.
- Los endpoints de administración viven en el módulo dueño del dato (`/admin/users` en identity,
  `/admin/moderation` en community...), sin un módulo «admin».
- [Spring Modulith](https://spring.io/projects/spring-modulith) verifica las fronteras en cada build
  (`ModularityTests`) y genera el mapa de módulos ([`docs/modules.md`](../modules.md)). ArchUnit añade reglas de
  diseño (`ArchitectureTests`).

## Consecuencias

- Una funcionalidad se entiende y se cambia dentro de su carpeta; las dependencias entre módulos son explícitas y
  no hay ciclos.
- Cruzar módulos cuesta algo más: hace falta una interfaz pública (`Entitlements`, `UserAccounts`...) o un evento
  en lugar de navegar por entidades.
- Si algún día un módulo necesita escalar por separado, sus fronteras ya están marcadas y su comunicación con el
  resto pasa por interfaces y eventos.

## Alternativas descartadas

- **Mantener las capas técnicas.** Es el origen de los problemas descritos.
- **Microservicios.** Coste operativo desproporcionado para el tamaño del producto y del equipo.
- **Módulos Maven o JPMS.** Fronteras más rígidas, pero más fricción en el build; Modulith da la misma
  verificación con un solo artefacto.
