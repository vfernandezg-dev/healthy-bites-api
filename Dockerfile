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

# La app crea aquí la carpeta de subidas al arrancar; el usuario sin privilegios necesita poder escribir.
RUN chown healthybites:healthybites /app
USER healthybites

ENV PORT=8090
EXPOSE 8090
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "application.jar"]
