# Сборка модулей проекта
#
# Параметры:
# MODULE_NAME - имя модуля
# EXPOSE_PORT - порт для открытия командой EXPOSE (по умолчанию 8080)
# USAGE_MODULE - модуль со спецификацией для OpenAPI генерации используемого клиента
#                в файле src/main/resources/api-spec.yaml
# USAGE_MODULE2 - аналогично USAGE_MODULE
# USAGE_MODULE3 - аналогично USAGE_MODULE

# Этап 1 - Сборка
FROM eclipse-temurin:21-jdk-jammy AS builder

ARG MODULE_NAME
ARG USAGE_MODULE=${MODULE_NAME}
ARG USAGE_MODULE2=${MODULE_NAME}
ARG USAGE_MODULE3=${MODULE_NAME}

WORKDIR /app
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY ${MODULE_NAME}/pom.xml ${MODULE_NAME}/pom.xml
COPY ${USAGE_MODULE}/src/main/resources/*.yaml ${USAGE_MODULE}/src/main/resources/
COPY ${USAGE_MODULE2}/src/main/resources/*.yaml ${USAGE_MODULE2}/src/main/resources/
COPY ${USAGE_MODULE3}/src/main/resources/*.yaml ${USAGE_MODULE3}/src/main/resources/

# кэширование зависимостей
WORKDIR /app/${MODULE_NAME}
RUN /app/mvnw dependency:go-offline -B

# сборка
COPY shared ../shared
COPY ${MODULE_NAME}/src src
RUN /app/mvnw package -am -Dmaven.test.skip=true

# Этап 2 - Образ для запуска приложения
FROM eclipse-temurin:21-jre-jammy

ARG MODULE_NAME
ARG EXPOSE_PORT=8080
WORKDIR /app
COPY --from=builder /app/${MODULE_NAME}/target/${MODULE_NAME}-*.jar app.jar

EXPOSE ${EXPOSE_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
