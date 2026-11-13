# ADR 0011 · Arranque rápido en funciones y base de datos que se suspende

- **Estado:** aceptada
- **Fecha:** 2026-11-13

## Contexto

En producción la API corre como función de Vercel (Container runtime): con 1 vCPU, la instancia se pausa entre
peticiones y se apaga tras 5 minutos sin tráfico. PostgreSQL está en Neon, cuyo plan gratuito suspende la base de
datos tras 5 minutos sin consultas y da 100 CU-horas al mes.

Con la imagen anterior, Spring Boot tardaba 24-31 s en arrancar con 1 vCPU. En frío, Vercel cortaba la petición a
los ~30 s con `FUNCTION_INVOCATION_FAILED` y la siguiente volvía a empezar: abrir la app tras un rato sin uso
costaba uno o dos minutos de errores.

Además, si la instancia sigue viva, lo que consulte la base de datos de forma periódica impide que Neon se suspenda:
el monitor de eventos de Spring Modulith lo hacía cada minuto y el pool de HikariCP mantenía conexiones abiertas.
Sin suspensión, Neon gastaría unas 180 CU-horas al mes y se cortaría a mitad de mes.

## Decisión

- **Arranque en unos 9 s con 1 vCPU** (medido con `docker run --cpus=1 --memory=2g`):
  - recolector Serial y solo el compilador C1 (`-XX:+UseSerialGC -XX:TieredStopAtLevel=1`): de 24-31 s a 14 s;
  - caché AOT de Java 25 (JEP 483, 514 y 515) generada en el `Dockerfile` con una ejecución de entrenamiento
    (`spring.context.exit=onRefresh`) contra un PostgreSQL 18 temporal que se instala y se borra en la misma capa:
    de 14 s a unos 9 s. La imagen crece unos 150 MB.
- **Una instancia caliente.** Un workflow programado de `vfernandezg-infra` (`healthy-bites-keep-warm.yml`) llama
  cada 5 minutos, dos veces con 2,5 minutos de separación, a `/actuator/health/liveness`, que no toca la base de
  datos. Entre peticiones la instancia está pausada y no consume CPU ni memoria facturable. (Se pensó en un
  Cloudflare Worker, pero el token de Terraform no puede crear Workers.)
- **La base de datos puede suspenderse** aunque la instancia viva:
  - HikariCP sin conexiones en reposo (`minimum-idle: 0`, `idle-timeout` de 1 minuto, sin `keepalive`);
  - el monitor de eventos atascados comprueba cada hora (`spring.modulith.events.staleness.check-interval`);
  - las tareas horarias (vencimiento de suscripciones, reintento de eventos y limpieza de límites) corren juntas en
    el minuto 5, así despiertan la base de datos una sola vez por hora.

## Consecuencias

- Un arranque en frío ya no falla: la primera petición tarda lo que el arranque (7-8 s en Vercel según sus logs;
  hasta 15 s si arrancan muchas instancias a la vez) y responde.
- La primera consulta tras un minuto sin uso abre una conexión nueva (100-300 ms) y, si Neon estaba suspendido, lo
  despierta (alrededor de 1 s).
- Las tareas programadas solo avanzan mientras la instancia atiende peticiones (el ping incluido): son de mejor
  esfuerzo y ShedLock evita que dos instancias las repitan.
- El entrenamiento debe repetirse con la imagen: si cambian la JVM o las dependencias, la caché vieja no se usa (la
  JVM la ignora y arranca normal, solo que más lenta).

## Alternativas descartadas

- **Inicialización diferida** (`spring.main.lazy-initialization`): apenas 1 s menos y la primera petición pagaba
  el resto.
- **Imagen nativa (GraalVM):** arranque de décimas, pero compilaciones largas, configuración de reflexión para
  Hibernate y Spring Modulith y depuración más difícil.
- **Un servidor siempre encendido** (Render, Fly.io): elimina el problema, pero cuesta dinero para un proyecto de
  portafolio.
