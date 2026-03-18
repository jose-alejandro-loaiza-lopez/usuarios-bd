FROM amazoncorretto:21-alpine
LABEL authors="josea"
WORKDIR /app
COPY target/usuarios-bd-1.0.jar /app
ENTRYPOINT ["java", "-jar", "usuarios-bd-1.0.jar"]