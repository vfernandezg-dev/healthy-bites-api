<div align="center">

<h1>Healthy Bites · API REST</h1>

<p><strong>Backend de la plataforma de nutrición, hábitos y comunidad Healthy Bites.</strong></p>

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-000000?logo=jsonwebtokens&logoColor=white)
![PayPal](https://img.shields.io/badge/Pagos-PayPal-003087?logo=paypal&logoColor=white)

[Módulos](#módulos) · [Ejecución local](#ejecución-local) · [Configuración](#configuración) ·
[Arquitectura](#arquitectura) · [Equipo](#equipo)

</div>

## Sobre el proyecto

Mantener hábitos saludables de forma constante es difícil sin herramientas que se adapten a cada
persona. **Healthy Bites** reúne en una sola plataforma planes alimenticios creados por
nutricionistas, contenido educativo, seguimiento de hábitos y metas, rachas con recompensas y una
comunidad para compartir avances.

Este repositorio contiene la **API REST** que da servicio a toda la plataforma. Es un proyecto
académico que forma parte de un sistema de tres repositorios:

| Repositorio                                                                       | Descripción                                          |
| --------------------------------------------------------------------------------- | ---------------------------------------------------- |
| [healthy-bites-api](https://github.com/vfernandezg-dev/healthy-bites-api)         | API REST en Spring Boot + PostgreSQL (este repo)     |
| [healthy-web-app](https://github.com/vfernandezg-dev/healthy-web-app)             | Aplicación web en Angular                            |
| [healthy-bites-landing](https://github.com/vfernandezg-dev/healthy-bites-landing) | Landing page en Astro (usa el endpoint de contacto)  |

## Módulos

La API tiene tres roles: **CLIENTE**, **NUTRICIONISTA** y **ADMIN**. Todas las rutas cuelgan de
`/api/v1`.

| Módulo                      | Qué permite                                                                                       | Rutas principales                                        |
| --------------------------- | ------------------------------------------------------------------------------------------------- | -------------------------------------------------------- |
| Autenticación y perfil      | Registro de clientes y nutricionistas, login con JWT, recuperación de contraseña por correo       | `/auth`, `/mail`, `/user/profile`                        |
| Planes alimenticios         | Planes por objetivo (déficit, mantenimiento, masa muscular) con comidas diarias                   | `/plan-alimenticio`, `/acceso-plan`                      |
| Contenido                   | Videos, artículos y guías de nutricionistas, gratuitos o para suscriptores; subida de archivos    | `/contenido`, `/acceso-contenido`, `/media`              |
| Hábitos y progreso          | Registro de hidratación, alimentación, ejercicio y sueño; metas de peso y su seguimiento           | `/habito`, `/meta`, `/meta/{metaId}/seguimientos`        |
| Rachas y recompensas        | Cada día con hábitos registrados suma a la racha; al completarla se desbloquea la recompensa      | `/racha`, `/recompensa`                                  |
| Comunidad                   | Grupos de interés, publicaciones y comentarios                                                    | `/grupo`, `/unirse`, `/publicacion`, `/comentario`       |
| Suscripciones y pagos       | Planes Básico, Premium y VIP; pago con PayPal y correo de confirmación                            | `/suscripcion`, `/checkout`                              |
| Administración              | Gestión completa de clientes, nutricionistas, planes, contenido, grupos, metas y recompensas      | `/admin/**`                                              |
| Contacto                    | Formulario público de la landing page                                                             | `/contact`                                               |

La documentación interactiva (OpenAPI) está disponible en
`http://localhost:8090/api/v1/swagger-ui/index.html` al ejecutar la API en local.

## Stack

| Área          | Tecnología                                                                   |
| ------------- | ---------------------------------------------------------------------------- |
| Lenguaje      | Java 21                                                                      |
| Framework     | Spring Boot 3.3 (Web, Data JPA, Security, Validation, Mail, Thymeleaf)       |
| Base de datos | PostgreSQL 16 (Docker en desarrollo)                                         |
| Seguridad     | Spring Security con JWT (jjwt) y contraseñas con BCrypt                      |
| Integraciones | PayPal REST API (sandbox), SMTP (Gmail) con plantillas Thymeleaf             |
| Utilidades    | ModelMapper, Lombok, springdoc-openapi (Swagger UI)                          |
| Pruebas       | JUnit 5, Mockito, MockMvc                                                    |
| Build         | Maven Wrapper; imagen Docker (`Dockerfile`)                                  |

## Ejecución local

### Requisitos

- **JDK 21.** Spring Boot 3.3 con Lombok no compila con JDK 25.
- **Docker**, para la base de datos.

### Pasos

```bash
# 1. Base de datos PostgreSQL (se publica en el puerto 5433)
docker compose up -d

# 2. Variables de entorno (credenciales SMTP, etc.)
cp .env.example .env

# 3. Arrancar la API en http://localhost:8090/api/v1
.\run-backend.ps1          # Windows (PowerShell): fija JDK 21 y carga .env
./mvnw spring-boot:run      # macOS/Linux, con JAVA_HOME apuntando a JDK 21
```

Al arrancar se cargan datos de demostración ([`data-test.sql`](src/main/resources/data-test.sql)).
Todas las cuentas usan la contraseña `healthy123`:

| Rol           | Cuenta                                                             |
| ------------- | ------------------------------------------------------------------ |
| Administrador | `admin@healthybites.com`                                           |
| Nutricionista | `carlos.garcia@healthybites.com`, `elena.gomez@healthybites.com`   |
| Cliente       | `juan.perez@healthybites.com` (plan Básico)                        |
| Cliente       | `maria.lopez@healthybites.com` (plan Premium)                      |
| Cliente       | `pedro.gomez@healthybites.com` (plan VIP)                          |

Para arrancar sin datos de demostración, usa `DB_SEED_MODE=never`.

### Pruebas

```bash
./mvnw verify
```

Solo necesitas Docker en marcha. `HealthyBitesApplicationTests` levanta la aplicación completa sobre
un PostgreSQL efímero con [Testcontainers](https://testcontainers.com) y el seed de demo. Comprueba de
extremo a extremo el login, el registro, un flujo de cliente, uno de nutricionista y el formulario de
contacto de la landing. El resto son pruebas unitarias y de controlador (Mockito y MockMvc).

CI ejecuta lo mismo en cada pull request ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)).

## Configuración

Los secretos no se versionan. Copia [`.env.example`](.env.example) como `.env`; `run-backend.ps1` lo
carga al arrancar. En producción, define las mismas variables en el proveedor de hosting.

| Variable                               | Por defecto              | Uso                                                      |
| -------------------------------------- | ------------------------ | -------------------------------------------------------- |
| `MAIL_USERNAME`                        | `healtybite3@gmail.com`  | Cuenta SMTP (Gmail) que envía los correos                |
| `MAIL_PASSWORD`                        | _(vacío)_                | Contraseña de aplicación de esa cuenta                   |
| `CONTACT_RECIPIENT`                    | `MAIL_USERNAME`          | Buzón que recibe los mensajes del formulario de contacto |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Postgres local en `5433` | Conexión a la base de datos                              |
| `DB_SEED_MODE`                         | `always`                 | `never` para no cargar datos de demostración             |
| `SERVER_PORT`                          | `8090`                   | Puerto HTTP                                              |
| `SHOW_SQL`                             | `false`                  | Muestra las consultas SQL en el log                      |

## Formulario de contacto

`POST /api/v1/contact` es público y lo usa la
[landing page](https://github.com/vfernandezg-dev/healthy-bites-landing).

```json
{ "nombre": "Ana", "apellido": "Torres", "correo": "ana@correo.com", "mensaje": "Hola…", "website": "" }
```

| Respuesta | Cuándo                                                                |
| --------- | --------------------------------------------------------------------- |
| `204`     | Mensaje enviado a `CONTACT_RECIPIENT`, con _reply-to_ al visitante    |
| `422`     | Datos inválidos (mismas reglas que valida la landing)                 |
| `429`     | Límite superado: 5 mensajes cada 10 min por IP y 50 por hora en total |
| `503`     | El servidor de correo no está disponible                              |

El campo `website` es un _honeypot_: si llega con valor, el mensaje se descarta y se responde `204`.
Nunca se envía un correo automático al visitante, así el endpoint no puede usarse para mandar spam a
terceros.

## Arquitectura

La API sigue una arquitectura en capas:

| Paquete       | Responsabilidad                                                                  |
| ------------- | -------------------------------------------------------------------------------- |
| `api`         | Controladores REST: reciben las peticiones HTTP y devuelven las respuestas       |
| `dto`         | Objetos de entrada y salida, con validación (Bean Validation)                    |
| `service`     | Interfaces con la lógica de negocio                                              |
| `service.impl` | Implementación de la lógica de negocio                                           |
| `repository`  | Acceso a datos con Spring Data JPA                                               |
| `model`       | Entidades JPA y enumeraciones                                                    |
| `mapper`      | Conversión entre entidades y DTO                                                 |
| `security`    | JWT, filtros de autenticación y límite de envíos del formulario de contacto      |
| `integration` | Servicios externos: PayPal y envío de correos                                    |
| `exception`   | Excepciones de dominio y manejador global de errores                             |
| `config`      | Seguridad, CORS, OpenAPI y ModelMapper                                           |

### Diagrama de clases

![Diagrama de clases](diagrama_de_clases.png)

### Diagrama de base de datos

![Diagrama de base de datos](diagrama_base_de_datos.png)

## Gestión del proyecto

El avance se organizó en un [tablero de Trello](https://trello.com/b/X7usPWHI/healty-bites) con las
columnas Backlog, En progreso, Revisión, En pruebas y Hecho.

## Equipo

Healthy Bites es un proyecto académico. **Valentin Fernandez** lideró el proyecto y desarrolló y
configuró la plataforma completa: esta API REST, la aplicación web (Angular), la landing (Astro), el
despliegue y la integración continua.

| Integrante         | Rol                        | Perfil                                                                    |
| ------------------ | -------------------------- | ------------------------------------------------------------------------- |
| Valentin Fernandez | Líder técnico · Full Stack | [LinkedIn](https://www.linkedin.com/in/vfernandezg-dev/)                  |
| Camila Moreno      | Diseño UI/UX               | [LinkedIn](https://www.linkedin.com/in/camila-moreno-quevedo-01871a324/)  |
| Alessia Aguilar    | Base de datos              | [LinkedIn](https://www.linkedin.com/in/alessia-aguilar-857670286/)        |
| Jhonnatan Chavez   | Arquitectura de software   | [LinkedIn](https://www.linkedin.com/in/jhonnatan-del-castillo-a73a24316/) |
| Renzo Lavado       | Comercial                  | [LinkedIn](https://www.linkedin.com/in/renzo-lavado-41aba2296/)           |
