<div align="center">

<h1>Healthy Bites · API REST</h1>

<p><strong>Backend de la plataforma de nutrición, hábitos y comunidad Healthy Bites.</strong></p>

![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?logo=postgresql&logoColor=white)
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

La documentación interactiva (OpenAPI) está en
`http://localhost:8090/api/v1/swagger-ui/index.html` al ejecutar la API en local. Por seguridad, solo se
publica en el perfil de desarrollo.

## Stack

| Área          | Tecnología                                                                   |
| ------------- | ---------------------------------------------------------------------------- |
| Lenguaje      | Java 25                                                                      |
| Framework     | Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Mail, Thymeleaf)   |
| Base de datos | PostgreSQL 18 (Docker Compose en desarrollo)                                 |
| Seguridad     | Spring Security (resource server): access token JWT + refresh token rotatorio en cookie; BCrypt; errores RFC 9457 |
| Integraciones | PayPal REST API (sandbox), SMTP con plantillas Thymeleaf (Mailpit en local)  |
| Utilidades    | ModelMapper, Lombok, springdoc-openapi (Swagger UI)                          |
| Pruebas       | JUnit 6, Mockito, MockMvc, Testcontainers (PostgreSQL)                       |
| Build         | Maven Wrapper; imagen Docker multi-stage (`Dockerfile`)                      |

## Ejecución local

### Requisitos

- **JDK 25** (por ejemplo, Temurin 25).
- **Docker**, para PostgreSQL y Mailpit.

### Pasos

```bash
# 1. Variables de entorno: copia la plantilla y rellena JWT_SECRET
#    (openssl rand -base64 64 | tr -d '\r\n') y, si vas a probar pagos, PayPal sandbox.
cp .env.example .env

# 2. Arrancar la API en http://localhost:8090/api/v1
./mvnw spring-boot:run
```

`spring-boot:run` usa el perfil `dev`, que:

- carga `.env`;
- levanta PostgreSQL (puerto 5433) y [Mailpit](https://mailpit.axllent.org) con
  [`docker-compose.yml`](docker-compose.yml) y conecta la base de datos sola;
- envía todos los correos a Mailpit, que los muestra en `http://localhost:8025` (nada sale a Internet);
- solo escucha en `127.0.0.1`, publica Swagger UI y carga los datos de demostración
  ([`data-test.sql`](src/main/resources/data-test.sql)).

Todas las cuentas de demostración usan la contraseña `healthy123`:

| Rol           | Cuenta                                                             |
| ------------- | ------------------------------------------------------------------ |
| Administrador | `admin@healthybites.com`                                           |
| Nutricionista | `carlos.garcia@healthybites.com`, `elena.gomez@healthybites.com`   |
| Cliente       | `juan.perez@healthybites.com` (plan Básico)                        |
| Cliente       | `maria.lopez@healthybites.com` (plan Premium)                      |
| Cliente       | `pedro.gomez@healthybites.com` (plan VIP)                          |

Los datos de demostración nunca se cargan fuera de los perfiles `dev` y `test`.

### Pruebas

```bash
./mvnw verify
```

Solo necesitas Docker en marcha. Los tests de integración levantan la aplicación completa sobre un
PostgreSQL efímero con [Testcontainers](https://testcontainers.com) y el seed de demo:

- `HealthyBitesApplicationTests`: login, registro, un flujo de cliente, uno de nutricionista y el
  formulario de contacto de la landing, de extremo a extremo.
- `CriticalSecurityFixesTests`: regresiones de los agujeros de seguridad cerrados.
- `ErrorResponsesTests`: formato de los errores.

El resto son pruebas unitarias y de controlador (Mockito y MockMvc).

CI ejecuta lo mismo en cada pull request, además de [gitleaks](https://github.com/gitleaks/gitleaks)
para detectar secretos ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)).

## Configuración

Ningún secreto se versiona. Toda la configuración llega de variables de entorno: en local, del fichero
`.env` (plantilla documentada en [`.env.example`](.env.example)); en producción, del proveedor de hosting.
La aplicación **no arranca** si falta un secreto obligatorio o si `JWT_SECRET` es demasiado corto.

| Variable                                | Obligatoria       | Uso                                                              |
| --------------------------------------- | ----------------- | ---------------------------------------------------------------- |
| `JWT_SECRET`                            | Sí                | Clave HS512 de los JWT (Base64, ≥ 64 bytes)                      |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`  | Sí (en local, solo `DB_PASSWORD`) | Conexión a PostgreSQL                            |
| `MAIL_USERNAME`, `MAIL_PASSWORD`        | En producción     | Cuenta SMTP que envía los correos                                |
| `APP_FRONTEND_URL`, `API_PUBLIC_URL`    | En producción     | URLs públicas del web-app (enlaces de correo, PayPal) y de la API |
| `PAYPAL_CLIENT_ID`, `PAYPAL_CLIENT_SECRET` | Para pagos     | Credenciales de PayPal (sandbox)                                 |
| `CONTACT_RECIPIENT`                     | No                | Buzón del formulario de contacto (por defecto, `MAIL_USERNAME`)  |
| `STORAGE_LOCATION`                      | No                | Carpeta de los ficheros subidos (por defecto `uploads`)          |
| `CORS_APP_ORIGINS`, `CORS_LANDING_ORIGINS` | En producción  | Orígenes del web-app (toda la API, con credenciales) y de la landing (solo `/contact`) |
| `SERVER_TOMCAT_REMOTEIP_INTERNAL_PROXIES` | Según el hosting | IPs del proxy en las que confiar para `X-Forwarded-For`        |
| `PORT`                                  | No                | Puerto HTTP (por defecto `8090`)                                 |
| `JWT_ACCESS_TOKEN_TTL`, `SHOW_SQL`      | No                | Validez del access token (ISO-8601, `PT15M`) y log del SQL       |

## Sesión y seguridad

- **Login** (`POST /auth/login`) devuelve un **access token JWT** de 15 minutos en el cuerpo (el web-app lo guarda
  solo en memoria) y deja un **refresh token** en una cookie `HttpOnly; Secure; SameSite=Strict` limitada a
  `/api/v1/auth`.
- **`POST /auth/refresh`** cambia la cookie por un access token nuevo y rota el refresh token. Si llega un refresh
  token ya usado, se revoca toda la sesión (detección de robo), salvo en los 20 s siguientes a la rotación (dos
  pestañas refrescando a la vez). Las sesiones caducan a los 7 días sin uso y, como máximo, a los 30.
- **`POST /auth/logout`** revoca la sesión y borra la cookie. Refresh y logout exigen la cabecera
  `X-Requested-With` y un `Origin` permitido (defensa CSRF además de SameSite).
- Los refresh tokens se guardan solo como hash SHA-256. El login tiene límite de intentos fallidos (por cuenta e IP,
  con `Retry-After`).
- CORS por listas blancas, cabeceras de seguridad (CSP, `nosniff`, `Referrer-Policy`, `X-Frame-Options`) y Actuator
  limitado a `/actuator/health` (liveness y readiness).

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
