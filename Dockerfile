# Usar una imagen base ligera de OpenJDK 21 para ejecutar la aplicación Java
FROM openjdk:21-jdk-slim

# Define la variable del archivo JAR
ARG JAR_FILE=target/healthy-bites-1.0.0.jar

# Copia el archivo JAR al contenedor
COPY ${JAR_FILE} healthy-bites.jar

# Expone el puerto 8080
EXPOSE 8080

# Comando para ejecutar el archivo JAR
ENTRYPOINT ["java","-jar","healthy-bites.jar"]