FROM eclipse-temurin:17-jdk-alpine
LABEL maintainer="Caltias - PymeTrack"
WORKDIR /app
# Copiamos el JAR que generaremos con Maven
COPY target/*.jar app.jar
# Exponemos el puerto de notificaciones
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]