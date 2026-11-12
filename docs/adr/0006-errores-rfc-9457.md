# ADR 0006 · Errores RFC 9457 con código estable

- **Estado:** aceptada
- **Fecha:** 2026-11-11

## Contexto

La versión 1 respondía los errores con formatos distintos según el controlador, a veces con el mensaje de la
excepción (que filtraba detalles internos), y un login fallido devolvía `500`. El web-app tenía que interpretar
textos para saber qué había pasado.

## Decisión

- Todos los errores siguen [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) (`application/problem+json`, el
  `ProblemDetail` de Spring) con:
  - `code`: identificador estable en kebab-case (`subscription-required`, `active-goal-exists`,
    `validation-failed`...), que es lo que interpreta el web-app;
  - `detail`: mensaje en español para mostrar a la persona;
  - `errors[{field, message}]` cuando hay campos inválidos;
  - datos extra cuando ayudan, como `requiredTier` en `403 subscription-required`.
- Los servicios lanzan `ApiException` con su código; `GlobalExceptionHandler` traduce también las excepciones de
  Spring (validación, JSON mal formado, seguridad).
- Una excepción inesperada responde `500` con un mensaje genérico y se registra en el log con el `X-Request-Id`:
  nunca se devuelve su mensaje.

## Consecuencias

- El web-app trata los errores de forma uniforme: un interceptor muestra `detail` y coloca `errors[]` en los campos
  del formulario.
- Los tests comprueban el `code`, no el texto, así que los mensajes se pueden mejorar sin romper nada.
- Añadir un error nuevo es elegir un `code` y un mensaje; queda documentado en el contrato OpenAPI.

## Alternativas descartadas

- **Formato propio.** Reinventa lo que el estándar ya resuelve y Spring ya implementa.
- **Solo el código HTTP.** No distingue, por ejemplo, «ya tienes una meta activa» de «la meta no es tuya».
