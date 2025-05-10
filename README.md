# ibank: Демонстрационное приложение «Банк» с использованием Spring Boot и паттернов микросервисной архитектуры

Общие сервисы:
1. Externalized/Distributed Config: Spring Cloud Config (конфиги в локальном каталоге) (confsrv)
2. Service Discovery: Eureka (eureka)
3. Gateway API: Spring Cloud Gateway (gateway)
4. Сервер авторизации микросервисов по OAuth 2.0: Keycloak
5. БД: PostgreSQL (одна БД, отдельные независимые схемы на каждый сервис)

Прикладные микросервисы:
1. фронта (Front UI) (front-service);
2. сервиса аккаунтов (Accounts) (accounts-service);
3. сервиса обналичивания денег (Cash);
4. сервиса перевода денег между счетами одного или двух аккаунтов (Transfer);
5. сервиса конвертации валют (Exchange);
6. сервиса генерации курсов валют (Exchange Generator);
7. сервиса блокировки подозрительных операций (Blocker);
8. сервиса уведомлений (Notifications).


## Установка приложения в Docker

В случае доступности Docker Compose можно установить и запустить приложение командой:

```cmd
  docker compose up --build --detach
```
Интрефейс будет доступен по URL:

[http://localhost:8880](http://localhost:8880)

Команда для остановки и удаления приложения:

```cmd
  docker compose down
```


## Запуск отдельных сервисов в Docker

Команда для сборки образа в выполняется в корневом каталоге проекта, в MODULE_NAME указывается имя модуля, в EXPOSE_PORT открытый порт (можно не указывать если 8080).

Пример сборки и запуска сервиса Spring Cloud Config (модуль confsrv):

```cmd
  docker build --build-arg MODULE_NAME=confsrv --build-arg EXPOSE_PORT=8888 -t ibank-confsrv:manual . && docker run -p 8888:8888 ibank-confsrv:manual
```

Настраиваемые параметры для сборки и запуска сервисов можно посмотреть в docker-compose.yml.


## Запуск тестов

Предварительные требования:
- Java 21 (например, Eclipse Temurin OpenJDK 21.0.5+11)
- Docker (для сборки образов и запуска сервисов при тестировании)

Команда для выполнения тестов:

```cmd
  ./mvnw clean verify
```
