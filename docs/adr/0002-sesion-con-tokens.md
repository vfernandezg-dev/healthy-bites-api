# ADR 0002 · Sesión: access token en memoria + refresh token rotatorio

- **Estado:** aceptada
- **Fecha:** 2026-11-11

## Contexto

La versión 1 emitía un JWT de 30 días que el web-app guardaba en `localStorage`: cualquier XSS podía robarlo y no
había forma de revocarlo. El filtro que debía validarlo se registraba tres veces y nunca comprobaba la firma.

El web-app (Angular) y la API se desplegarán en subdominios del mismo dominio, y la landing envía su formulario de
contacto desde otro origen.

## Decisión

- **Access token** JWT firmado con HS512, de 15 minutos, con `sub` (UUID del usuario), `roles`, `iss`, `aud` y
  `jti`. El web-app lo guarda **solo en memoria** y lo envía como `Authorization: Bearer`. La API lo valida con el
  resource server de Spring Security (Nimbus), comprobando firma, emisor, audiencia y caducidad.
- **Refresh token** opaco de 256 bits en una cookie `__Secure-hb_rt` (`HttpOnly; Secure; SameSite=Strict`,
  `Path=/api/v1/auth`, solo para el host de la API). En la base de datos solo se guarda su hash SHA-256, con su
  familia, caducidad y revocación.
- **Rotación en cada uso.** `POST /auth/refresh` emite un refresh token nuevo y marca el anterior como reemplazado.
  Si llega uno ya reemplazado, se revoca toda la familia (alguien lo ha copiado), salvo en una ventana de 20 s que
  cubre dos pestañas refrescando a la vez: en ella solo se responde `401`.
- Caducidad de 7 días sin uso y 30 días como máximo. Desactivar una cuenta o cambiar la contraseña revoca todas
  sus sesiones.
- `refresh` y `logout` exigen `X-Requested-With` y un `Origin` permitido, porque los subdominios hermanos son
  *same-site* y `SameSite` no basta contra CSRF entre ellos.

## Consecuencias

- Un XSS ya no puede llevarse una sesión de larga duración: el access token vive 15 minutos y el refresh token no
  es accesible desde JavaScript.
- Al recargar la página, el web-app recupera la sesión con un refresh silencioso. Las pestañas coordinan el refresh
  con Web Locks para no gastar el mismo token dos veces.
- Los ficheros protegidos no pueden pedirse con `<img>` o `<video>` (no envían el bearer): se sirven con URLs
  firmadas de vida corta.
- Hay estado en el servidor (`refresh_tokens`), a cambio de poder revocar sesiones.

## Alternativas descartadas

- **JWT de larga duración en `localStorage`.** Es el problema de partida.
- **Sesión de servidor con cookie (Spring Session).** Válida, pero obliga a protección CSRF en toda la API y a
  compartir la cookie entre orígenes; el access token en memoria mantiene la API sin estado salvo en `/auth`.
- **Refresh token sin rotación.** Un token robado serviría hasta caducar sin que nadie lo detectara.
