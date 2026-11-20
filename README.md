# Habit Tracker

Микросервисное приложение для отслеживания привычек: пользователи создают привычки, данные хранятся в PostgreSQL.

## Содержание

- [Стек](#стек)
- [Архитектура](#архитектура)
- [Структура проекта](#структура-проекта)
- [Конфигурация и секреты](#конфигурация-и-секреты)
- [Быстрый старт](#быстрый-старт)
- [API](#api)
- [Переменные окружения](#переменные-окружения)
- [jOOQ и схема БД](#jooq-и-схема-бд)
- [Разработка](#разработка)
- [Диаграммы](#диаграммы)

Отдельные документы:

- [Как запустить: Docker, локально, на сервере](docs/RUNBOOK.md)
- [Стек технологий: что это, зачем и плюсы](docs/STACK.md)
- [Как запустить — полный гайд](docs/RUNBOOK.md)
- [Стек технологий — зачем и почем[[у]]](docs/STACK.md)

## Стек

| Компонент | Версия | Роль |
|-----------|--------|------|
| Java | 21 (LTS) | Runtime |
| Spring Boot | 3.3.5 | Основа сервисов, автоконфигурация |
| Spring Cloud Gateway | 2023.0.3 | Шлюз: маршруты и CORS только в YAML |
| Spring Cloud OpenFeign | 2023.0.3 | Декларативные HTTP-вызовы между сервисами |
| jOOQ | 3.19 | Типобезопасный SQL, кодогенерация из `db/init.sql` |
| PostgreSQL | 16 | Хранилище |
| HikariCP | (в составе Boot) | Пул соединений |
| Bean Validation | (в составе Boot) | Валидация запросов |
| Actuator | (в составе Boot) | Health/метрики |
| Docker Compose | v2 | Локальный запуск стека |

Что это за технологии, зачем они в проекте и в чём их плюсы — [docs/STACK.md](docs/STACK.md).

## Архитектура

```text
Клиент -> API Gateway :8080 -> user-service     :8081 -> PostgreSQL :5432
                             -> habit-service    :8082 -> PostgreSQL :5432
                             -> tracking-service :8083 -> PostgreSQL :5432
                                     |                  |
                                     |                  +--Feign--> habit-service (проверка привычки)
                                     +--Feign--> user-service (проверка пользователя)
```

- **api-gateway** — Spring Cloud Gateway. Ни строчки проксирующего кода: маршруты, CORS и таймауты описаны в `application.yml`.
- **user-service** — пользователи (создание, список, получение по id).
- **habit-service** — привычки; перед созданием проверяет пользователя через Feign-клиент.
- **tracking-service** — отметки о выполнении привычек; перед созданием проверяет привычку через Feign-клиент.
- **PostgreSQL** — общая БД, схема создаётся из `db/init.sql`.

Внутри каждого сервиса — слоистая доменная структура: `domain` → `application` → `infrastructure` / `api`.
Подробнее по C4 — в [docs/architecture/ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md).

## Структура проекта

```text
habit-tracker/
├── api-gateway/            # Spring Cloud Gateway (только конфигурация)
├── user-service/
├── habit-service/
├── tracking-service/
├── config/secrets/         # локальные креды (gitignored), в git — *.example
├── db/init.sql             # схема БД + источник для jOOQ-кодогенерации
├── docs/architecture/      # C4-описание + PlantUML-диаграммы
├── docker-compose.yml
└── pom.xml                 # родительский POM (Spring Boot parent)
```

Слои внутри сервиса:

```text
src/main/java/com/habittracker/<service>/
├── <Service>Application.java      # @SpringBootApplication (+@EnableFeignClients)
├── domain/
│   ├── model/                     # агрегаты (record)
│   ├── repository/                # порты хранилища (интерфейсы)
│   ├── port/                      # прочие порты (habit -> user, tracking -> habit)
│   └── exception/                 # доменные исключения
├── application/                   # сценарии использования, @Transactional
├── infrastructure/
│   ├── persistence/               # адаптеры jOOQ (+ сгенерированный пакет jooq/)
│   └── client/                    # Feign-клиент и его адаптер
└── api/                           # REST-контроллеры, DTO, @RestControllerAdvice
```

Сгенерированный jOOQ-код лежит в `target/generated-sources/jooq` и в git не попадает.

## Конфигурация и секреты

Всё, что раньше читалось из `System.getenv` вручную, теперь стандартный Boot-конфиг:
`src/main/resources/application.yml` + плейсхолдеры с дефолтами.

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/app}
    username: ${DB_USER:postgres}
    password: "${DB_PASS:}"
```

Пароль и логин не хранятся ни в коде, ни в `docker-compose.yml`. Они лежат в отдельном файле,
который подключается как дополнительный `PropertySource`:

```yaml
spring:
  config:
    import: optional:file:${CREDENTIALS_FILE:./config/secrets/db-credentials.properties}
```

Файл `config/secrets/db-credentials.properties`:

```properties
DB_USER=?
DB_PASS=?
```

Он в `.gitignore`. В git попадают только `.example`-шаблоны.

В Docker файл кред монтируется в контейнер read-only (`/run/secrets/db-credentials.properties`),
а `CREDENTIALS_FILE` указывает на этот путь — переменных `DB_PASS` в окружении нет.
Пароль самого PostgreSQL передаётся файлом через `POSTGRES_PASSWORD_FILE`.

## Быстрый старт

Нужен Docker Desktop с Compose v2.

```bash
cp config/secrets/db-credentials.properties.example config/secrets/db-credentials.properties
cp config/secrets/postgres-password.txt.example      config/secrets/postgres-password.txt
# отредактируйте оба файла: пароль в них должен совпадать
```

```bash
docker compose up -d --build
docker compose ps
curl http://localhost:8080/actuator/health
```

Если база уже создавалась со старой схемой — пересоздайте том:

```bash
docker compose down -v
docker compose up -d --build
```

Наружу опубликован только шлюз (`8080`). `user-service`, `habit-service` и `tracking-service` доступны лишь
внутри сети `habit-network`, порт БД на хост не проброшен (при необходимости раскомментируйте в `docker-compose.yml`).

Локальный запуск без Docker, развёртывание на сервере, бэкапы и диагностика — в [docs/RUNBOOK.md](docs/RUNBOOK.md).

## API

Все запросы — через шлюз `http://localhost:8080`.

| Метод | Путь | Описание | Ответ |
|-------|------|----------|-------|
| GET | `/users` | Список пользователей | `200 [...]` |
| GET | `/users/{id}` | Пользователь по id | `200 {...}` / `404` |
| POST | `/users/create` | Создать пользователя | `201 {...}` |
| GET | `/habits` | Список привычек | `200 [...]` |
| GET | `/habits/{id}` | Привычка по id | `200 {...}` / `404` |
| POST | `/habits/create` | Создать привычку | `201 {...}` |
| GET | `/trackings` | Все отметки | `200 [...]` |
| GET | `/trackings?habitId=1` | Отметки конкретной привычки | `200 [...]` |
| POST | `/trackings/create` | Отметить выполнение | `201 {...}` |

Примеры:

```bash
curl -X POST http://localhost:8080/users/create \
  -H "Content-Type: application/json" \
  -d '{"name":"Ivan","email":"ivan@test.com"}'

curl http://localhost:8080/users/1
```

```json
{"id":1,"name":"Ivan","email":"ivan@test.com"}
```

```bash
curl -X POST http://localhost:8080/habits/create \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"title":"Drink water"}'
```

```json
{"id":1,"userId":1,"title":"Drink water"}
```

```bash
curl -X POST http://localhost:8080/trackings/create \
  -H "Content-Type: application/json" \
  -d '{"habitId":1,"completedOn":"2026-11-20"}'
```

```json
{"id":1,"habitId":1,"completedOn":"2026-11-20"}
```

Ошибки — единый формат `{"error":"..."}`:

| Код | Когда |
|-----|-------|
| 400 | Невалидный JSON, не прошла валидация, дата в будущем |
| 404 | Пользователь или привычка не найдены |
| 409 | Email уже занят / привычка уже отмечена в этот день |
| 422 | Указанный `userId` или `habitId` не существует |
| 500 / 504 | Шлюз не смог подключиться к сервису / сервис не ответил по таймауту |

## Переменные окружения

| Переменная | Сервис | По умолчанию | Назначение |
|------------|--------|--------------|------------|
| `PORT` | все | 8080 / 8081 / 8082 / 8083 | HTTP-порт |
| `DB_URL` | user, habit, tracking | `jdbc:postgresql://localhost:5432/app` | JDBC URL |
| `DB_USER` / `DB_PASS` | user, habit, tracking | — | Читаются из файла кред |
| `CREDENTIALS_FILE` | user, habit, tracking | `./config/secrets/db-credentials.properties` | Путь к файлу кред |
| `DB_POOL_SIZE` | user, habit, tracking | `10` | Размер пула HikariCP |
| `DB_CONNECTION_TIMEOUT_MS` | user, habit, tracking | `5000` | Таймаут получения соединения |
| `USER_SERVICE_URL` | gateway, habit | `http://localhost:8081` | Адрес user-service |
| `HABIT_SERVICE_URL` | gateway, tracking | `http://localhost:8082` | Адрес habit-service |
| `TRACKING_SERVICE_URL` | gateway | `http://localhost:8083` | Адрес tracking-service |
| `CORS_ALLOWED_ORIGIN` | gateway | `*` | Разрешённый origin |
| `GATEWAY_RESPONSE_TIMEOUT` | gateway | `10s` | Таймаут ответа upstream |
| `FEIGN_CONNECT_TIMEOUT_MS` / `FEIGN_READ_TIMEOUT_MS` | habit, tracking | 3000 / 5000 | Таймауты Feign |

## jOOQ и схема БД

Таблицы генерируются из `db/init.sql` на этапе `generate-sources` (`DDLDatabase`), поэтому для сборки
база не нужна. Сгенерированный код появляется в `target/generated-sources/jooq`
и в git не коммитится. Каждый сервис генерирует только свои таблицы (`includes` в pom).

Схема:

```sql
users    (id, name, email UNIQUE)
habits   (id, user_id -> users.id, title)
trackings(id, habit_id -> habits.id, completed_on DATE, UNIQUE (habit_id, completed_on))
```

Схема меняется в одном месте — `db/init.sql`; после правки достаточно пересобрать сервисы:

```bash
mvn clean package
```

Драйвер диалекта задаётся в `application.yml` (`spring.jooq.sql-dialect: POSTGRES`).

## Разработка

```bash
mvn clean package      # сборка + кодогенерация + тесты
mvn test               # только тесты
```

Требуется JDK 21+ (проект собирается с `--release 21`).

Локальный запуск без Docker (порядок сервисов, переменные, `CREDENTIALS_FILE`) — в
[docs/RUNBOOK.md](docs/RUNBOOK.md). Коротко: соберите jar и запускайте из корня репозитория.

```bash
mvn clean package -DskipTests
java -jar user-service/target/user-service-1.0.0.jar
```

## Диаграммы

Архитектура описана в [docs/architecture/ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md),
диаграммы — PlantUML (`docs/architecture/diagrams/*.puml`) с отрисованными `.svg` рядом:

| Диаграмма | Файл |
|-----------|------|
| C4 L1 — System Context | [`context.svg`](docs/architecture/diagrams/context.svg) |
| C4 L2 — Containers | [`container.svg`](docs/architecture/diagrams/container.svg) |
| C4 L3 — user-service | [`component-user.svg`](docs/architecture/diagrams/component-user.svg) |
| C4 L3 — tracking-service | [`component-tracking.svg`](docs/architecture/diagrams/component-tracking.svg) |
| Sequence — создание отметки | [`sequence-create-tracking.svg`](docs/architecture/diagrams/sequence-create-tracking.svg) |

Перерисовать после правки `.puml`:

```bash
java -jar plantuml.jar -tsvg docs/architecture/diagrams
```
