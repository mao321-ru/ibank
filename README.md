# ibank: Демонстрационное приложение «Банк» с использованием Spring Boot и паттернов микросервисной архитектуры


## Описание

Общие сервисы:
1. Externalized/Distributed Config: Spring Cloud Config (конфиги в локальном каталоге) (confsrv)
2. Service Discovery: Eureka (eureka)
3. Gateway API: Spring Cloud Gateway (gateway)
4. Сервер авторизации микросервисов по OAuth 2.0: Keycloak (keycloak)
5. БД: PostgreSQL (одна БД, отдельные независимые схемы на каждый сервис) (postgres)

Прикладные микросервисы:
1. фронта (Front UI) (front-service);
2. сервиса аккаунтов (Accounts) (accounts-service);
3. сервиса обналичивания денег (Cash) (cash-service);
4. сервиса перевода денег между счетами одного или двух аккаунтов (Transfer) (transfer-service);
5. сервиса конвертации валют (Exchange) (exchange-service);
6. сервиса генерации курсов валют (Exchange Generator) (exrate-service);
7. сервиса блокировки подозрительных операций (Blocker) (blocker-service);
8. сервиса уведомлений (Notifications) (notify-service);

### Структура проекта

- common-test

Общие классы и настройки, применяемые при тестировании модулей.

- confsrv

Сервис Spring Cloud Config, файлы настроек находятся в confsrv/src/main/resources/config-repo/.

- eureka

Service Discovery: Eureka

- gateway

Spring Cloud Gateway

- keycloak

Настройки realm для сервера Keycloak находятся в keycloak/import/ibank.realm.json.

- postgres

Скрипты создания БД для PostgreSQL находятся в postgres/init/.

- shared

Общие классы и настройки, используемые в модулях.

- \*-service

Прикладные микросервисы

- refresh-keycloak-realm.sh

Скрипт для обновления настроек в keycloak/import/ibank.realm.json на основе изменений в настройках, сделанных вручную в интерфейсе Keycloak и экспортированных в файл keycloak/import/ibank.export.json.

- docker-compose.yml, Dockerfile

Настройки для сборки и запуска в Docker.


## Установка приложения в Docker

В случае доступности Docker Compose можно установить и запустить приложение командой:

```cmd
  docker compose up --build --detach
```
Интрефейс будет доступен по URL:

[http://localhost:8880](http://localhost:8880)

Изначально после установки никаких пользователей в системе нет: нужно зарегистрировать нового пользователя для использования функционала (по ссылке [Регистрация](http://localhost:8880/signup) на странице логина).

Команда для остановки и удаления приложения:

```cmd
  docker compose down
```
Команда для остановки и удаления приложения вместе созданными данными БД PostgreSQL:

```cmd
  docker compose down -v
```

Команда для удаления созданных образов сервисов приложения:

```cmd
  docker image ls -q ibank-* | xargs docker image rm
```


## Запуск отдельных сервисов в Docker

Команда для сборки образа в выполняется в корневом каталоге проекта, в MODULE_NAME указывается имя модуля, в EXPOSE_PORT открытый порт (можно не указывать если 8080).

Пример сборки и запуска сервиса Spring Cloud Config (модуль confsrv):

```cmd
  docker build --build-arg MODULE_NAME=confsrv --build-arg EXPOSE_PORT=8888 -t ibank-confsrv:manual . && docker run -p 8888:8888 ibank-confsrv:manual
```

Настраиваемые параметры для сборки и запуска сервисов можно посмотреть в docker-compose.yml.


## Выполнение тестов

Предварительные требования:
- Java 21 (например, Eclipse Temurin OpenJDK 21.0.5+11)
- Docker (для сборки образов и запуска сервисов при тестировании)

Команда для выполнения тестов:

```cmd
  ./mvnw clean verify
```

При этом выполняются интеграционные тесты: для тестирования текущего модуля запускаются все используемые им сервисы с помощью Testcontainers. Образы прикладных сервисов создаются при выполнении `mvn verify` и сохраняются в локальном Docker с именами вида "local/ibank-%" (например "local/ibank-confsrv", "local/ibank-front-service"). Эти образы удаляются при выполнении

```cmd
  ./mvnw clean
```

Выполнение всех тестов занимает около 12 минут.

Для запуска тестов отдельного модуля можно использовать команду (на примере front-service):


```cmd
  ./mvnw test -pl front-service
```

при этом предварительно должна быть успешно выполнена общая команда `./mvnw verify` (либо отдельно для каждого используемого сервиса `./mvnw verify -pl account-service` например) для сборки образов сервисов.


## Локальный запуск сервисов

Предварительные требования:

- Keycloak 26.x (например, 26.1.3)

Авторизационный сервис должен быть доступен по localhost:8954, в него должены быть импортированы настройки realm ibank из файла keycloak/import/ibank.realm.json.

- PostgreSQL 17.x (например PostgreSQL 17.2)

БД должна быть доступна по localhost:5432, в ней должны быть созданы БД скриптам из postgres/init.


Сервисы запускаются стандартными командами, первыми должны быть запущены сервисы

- confsrv:

```cmd
  ./mvnw spring-boot:run -pl confsrv
```

- eureka:

```cmd
  ./mvnw spring-boot:run -pl eureka
```

Затем прикладные, например

- front-service:

```cmd
  ./mvnw spring-boot:run -pl front-service
```

- accounts-service:

```cmd
  ./mvnw spring-boot:run -pl accounts-service
```

Последним запускается

- gateway:

```cmd
  ./mvnw spring-boot:run -pl gateway
```
