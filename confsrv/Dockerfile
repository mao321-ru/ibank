FROM eclipse-temurin:21.0.5_11-jdk-alpine-3.21
WORKDIR /app
COPY target/confsrv-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
