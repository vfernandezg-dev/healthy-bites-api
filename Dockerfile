# syntax=docker/dockerfile:1

# ---------- Build: compila el jar y lo extrae por capas ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Primero solo lo necesario para resolver dependencias: esta capa se reutiliza mientras no cambie el pom.
COPY .mvn/ .mvn/
# El permiso de ejecución se fija aquí: si el contexto se sube desde Windows (p. ej. con la CLI de Vercel), se pierde.
COPY --chmod=755 mvnw ./
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp dependency:go-offline

COPY src/ src/
# El jar se renombra a application.jar: la extracción conserva el nombre y el ENTRYPOINT lo usa.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp package -Dmaven.test.skip=true \
    && cp target/healthy-bites-*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---------- Runtime: solo JRE, usuario sin privilegios ----------
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S healthybites && adduser -S -G healthybites healthybites
WORKDIR /app

# Una capa por tipo de contenido: las dependencias cambian poco y se cachean entre versiones.
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

# Opciones de la JVM para arrancar rápido con 1 vCPU (Vercel Functions): recolector Serial y solo el compilador C1
# (la API espera sobre todo a la base de datos, no necesita el C2). Las mismas en el entrenamiento y al ejecutar: la
# caché AOT solo se aprovecha entera si coinciden.
ENV JAVA_OPTS_FAST_START="-XX:+UseSerialGC -XX:TieredStopAtLevel=1"

# Caché AOT de Java 25 (JEP 483, 514 y 515): una ejecución de entrenamiento arranca el contexto de Spring y sale
# (spring.context.exit=onRefresh, sin servidor web ni tareas programadas) y deja en app.aot las clases ya cargadas y
# enlazadas y los perfiles de los métodos. Con 1 vCPU, el arranque baja de unos 14 s a unos 9 s.
#
# El entrenamiento usa un PostgreSQL 18 temporal (Flyway, Hibernate y Spring Modulith leen la base de datos al
# arrancar) que se instala, se usa y se borra en esta misma capa: no queda en la imagen. Los secretos son valores de
# relleno que solo existen en esta orden.
RUN apk add --no-cache postgresql18 postgresql18-contrib >/dev/null \
    && install -d -o postgres -g postgres /run/postgresql /tmp/aot-db \
    && su postgres -c "initdb -D /tmp/aot-db -U training --auth=trust >/dev/null" \
    && su postgres -c "pg_ctl -D /tmp/aot-db -o '-c listen_addresses=127.0.0.1' -w start >/dev/null" \
    && su postgres -c "createdb -h 127.0.0.1 -U training training" \
    && java -XX:AOTCacheOutput=app.aot $JAVA_OPTS_FAST_START \
        -Dspring.context.exit=onRefresh \
        -Dspring.profiles.active=prod \
        -DDB_URL=jdbc:postgresql://127.0.0.1:5432/training \
        -DDB_USERNAME=training \
        -DDB_PASSWORD=training \
        -DJWT_SECRET=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA \
        -DMAIL_USERNAME=training \
        -DMAIL_PASSWORD=training \
        -DAPP_FRONTEND_URL=https://app.invalid \
        -DAPI_PUBLIC_URL=https://api.invalid/api/v1 \
        -jar application.jar \
    && su postgres -c "pg_ctl -D /tmp/aot-db -m fast -w stop >/dev/null" \
    && apk del --no-cache postgresql18 postgresql18-contrib >/dev/null \
    && rm -rf /tmp/aot-db /run/postgresql

# La app crea aquí la carpeta de subidas al arrancar; el usuario sin privilegios necesita poder escribir.
RUN chown healthybites:healthybites /app
USER healthybites

ENV PORT=8090
EXPOSE 8090
# Forma shell (exec) para expandir JAVA_OPTS_FAST_START; exec deja a java como PID 1 y recibe el SIGTERM.
ENTRYPOINT ["sh", "-c", "exec java -XX:AOTCache=app.aot $JAVA_OPTS_FAST_START -XX:MaxRAMPercentage=75 -jar application.jar"]
