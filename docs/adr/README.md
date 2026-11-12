# Decisiones de arquitectura (ADR)

Cada ADR recoge una decisión importante: el contexto, lo que se decidió, sus consecuencias y las alternativas
descartadas. Una decisión aceptada no se edita; si cambia, se escribe un ADR nuevo que la sustituye.

| ADR                                       | Decisión                                                    | Estado   |
| ----------------------------------------- | ----------------------------------------------------------- | -------- |
| [0001](0001-monolito-modular.md)          | Monolito modular con Spring Modulith                        | Aceptada |
| [0002](0002-sesion-con-tokens.md)         | Sesión: access token en memoria + refresh token rotatorio   | Aceptada |
| [0003](0003-identificadores-uuidv7.md)    | Identificadores UUIDv7                                      | Aceptada |
| [0004](0004-esquema-con-flyway.md)        | Esquema con Flyway y Hibernate en modo `validate`           | Aceptada |
| [0005](0005-eventos-persistidos.md)       | Eventos persistidos entre módulos                           | Aceptada (en parte sustituida por 0010) |
| [0006](0006-errores-rfc-9457.md)          | Errores RFC 9457 con código estable                         | Aceptada |
| [0007](0007-contrato-openapi.md)          | Contrato OpenAPI versionado y tipos generados en el web-app | Aceptada |
| [0008](0008-almacenamiento-de-objetos.md) | Ficheros en un almacenamiento de objetos S3-compatible      | Aceptada |
| [0009](0009-rest-frente-a-graphql.md)     | REST estandarizado en lugar de GraphQL                      | Aceptada |
| [0010](0010-varias-instancias.md)         | Preparada para varias instancias                            | Aceptada |
