# ibank: Демонстрационное приложение «Банк» с использованием Spring Boot и паттернов микросервисной архитектуры


## Описание

Общие сервисы:
1. Сервер авторизации микросервисов по OAuth 2.0: Keycloak (keycloak)
2. БД: PostgreSQL (одна БД, отдельные независимые схемы на каждый сервис) (postgres)

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

- config

Каталог с настройками сервисов

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


## Выполнение тестов

Предварительные требования:
- Java 21 (например, Eclipse Temurin OpenJDK 21.0.5+11)
- Docker (для сборки образов и запуска сервисов при тестировании)

Команда для выполнения тестов:

```cmd
  ./mvnw clean verify
```

При этом выполняются интеграционные тесты: для тестирования текущего модуля запускаются все используемые им сервисы с помощью Testcontainers. Образы прикладных сервисов создаются при выполнении `mvn verify` и сохраняются в локальном Docker с именами вида "local/ibank-%" (например "local/ibank-front-service"). Эти образы удаляются при выполнении

```cmd
  ./mvnw clean
```

Выполнение всех тестов занимает около 10 минут.

Для запуска тестов отдельного модуля можно использовать команду (на примере front-service):


```cmd
  ./mvnw test -pl front-service
```

при этом предварительно должна быть успешно выполнена общая команда `./mvnw verify` (либо отдельно для каждого используемого сервиса `./mvnw verify -pl account-service` например) для сборки образов сервисов.


## Локальный запуск сервисов

Предварительные требования:

- Keycloak 26.x (например, 26.1.3)

Авторизационный сервис должен быть доступен по keycloak.url, указанному в config/application-local.yml, в него должены быть импортированы настройки realm ibank из файла keycloak/import/ibank.realm.json.

- PostgreSQL 17.x (например PostgreSQL 17.2)

БД должна быть доступна по postgres_host:postgres_port, указанным в config/application-local.yml, в ней должны быть созданы БД скриптам из postgres/init.


Прикладные сервисы запускаются типовыми командами, например

- front-service:

```cmd
  ./mvnw spring-boot:run -pl front-service
```

- accounts-service:

```cmd
  ./mvnw spring-boot:run -pl accounts-service
```

## Установка в локальный Kubernetes (в составе Docker Desktop)

Предварительные требования:

- установка Ingress-контроллер NGINX для маршрутизации внешних запросов к сервисам

Команда для установки (согласно [Ingress NGINX Controller: Installation Guide](https://kubernetes.github.io/ingress-nginx/deploy/#quick-start))

```
helm upgrade --install ingress-nginx ingress-nginx \
  --repo https://kubernetes.github.io/ingress-nginx \
  --namespace ingress-nginx --create-namespace
```

Если порт 80 занят на localhost можно настроить маршрутизацию на другом порту, отредактировав командой

```
kubectl edit -n ingress-nginx service ingress-nginx-controller
```

значение spec.ports.port (заменить 80 например на 8180)

Отменить установку можно командой:

```
helm uninstall ingress-nginx -n ingress-nginx
```

Для установки приложения в Kubernetes нужно выполнить команду:

```
helm dependency update ./chart &&  helm install ibank ./chart
```

Для отмены установки нужно выполнить команду:

```
helm uninstall ibank
```
