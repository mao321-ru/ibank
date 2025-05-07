# Сборка модулей проекта
#
# Параметры:
# MODULE_NAME - имя модуля
# EXPOSE_PORT - порт для открытия командой EXPOSE (по умолчанию 8080)
FROM eclipse-temurin:21-jdk-jammy AS builder

ARG MODULE_NAME
WORKDIR /app
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY ${MODULE_NAME}/src ${MODULE_NAME}/src
COPY ${MODULE_NAME}/pom.xml ${MODULE_NAME}
WORKDIR /app/${MODULE_NAME}
RUN /app/mvnw package -am -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-jammy

ARG MODULE_NAME
ARG EXPOSE_PORT=8080
WORKDIR /app
COPY --from=builder /app/${MODULE_NAME}/target/${MODULE_NAME}-*.jar app.jar

EXPOSE ${EXPOSE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]


