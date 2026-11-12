# ADR 0005 · Eventos persistidos entre módulos

- **Estado:** aceptada
- **Fecha:** 2026-11-11

## Contexto

Algunas reacciones cruzan módulos: registrar un hábito avanza las rachas de rewards, completar una racha merece un
correo, pagar una suscripción envía un justificante. Llamar directamente al otro módulo acopla el que publica al que
reacciona, y hacerlo dentro de la misma transacción convierte un fallo del SMTP en un error del caso de uso
principal.

## Decisión

- El módulo dueño del cambio publica un **evento de dominio** (`HabitLogged`, `RewardEarned`,
  `SubscriptionActivated`...) en su paquete `events`. El evento **solo lleva identificadores**: quien lo escucha
  consulta lo que necesite a través de la API pública del otro módulo.
- Los listeners son `@ApplicationModuleListener` de Spring Modulith: el evento se guarda en `event_publication` en
  la misma transacción que el cambio y se entrega **tras el commit**, en otro hilo y en una transacción propia.
- Si un listener falla, la publicación queda como fallida y `FailedEventRetryJob` la reintenta cada 15 minutos
  (hasta 5 intentos); al arrancar se reintentan todas las pendientes. Las completadas se borran.
- **Excepción:** los correos de restablecimiento de contraseña y de invitación llevan el token en claro, así que no
  pasan por eventos persistidos: los envía identity con un listener `AFTER_COMMIT` normal. Si fallan, la persona
  puede volver a pedirlos.

## Consecuencias

- El caso de uso principal no depende de sus efectos secundarios: un SMTP caído no impide pagar ni registrar
  hábitos, y el correo sale cuando vuelve.
- La entrega es *al menos una vez*: un listener puede ejecutarse dos veces tras un fallo, así que sus efectos deben
  ser idempotentes o inocuos (avanzar una racha comprueba la fecha; un correo repetido es tolerable).
- El mapa de quién publica y quién escucha se genera desde el código en [`docs/modules.md`](../modules.md).

## Alternativas descartadas

- **Llamadas directas entre módulos.** Acoplan al que publica con todos los que reaccionan.
- **`@TransactionalEventListener` sin persistir.** Si la aplicación se cae entre el commit y el listener, el
  efecto se pierde.
- **Un broker (Kafka, RabbitMQ).** Infraestructura extra innecesaria dentro de un único despliegue; Modulith puede
  externalizar eventos más adelante si hiciera falta.
