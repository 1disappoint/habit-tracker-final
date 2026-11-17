# Habit Tracker

Микросервисное приложение для отслеживания привычек: пользователи создают привычки, а система хранит их в PostgreSQL.

## Содержание

- [Архитектура](#архитектура)
- [Технологии](#технологии)
- [Структура проекта](#структура-проекта)
- [Быстрый старт](#быстрый-старт)
- [API](#api)
- [Переменные окружения](#переменные-окружения)
- [Схема базы данных](#схема-базы-данных)
- [Разработка](#разработка)
- [Скриншоты](#скриншоты)

## Архитектура

```text
Клиент -> API Gateway :8080 -> user-service     :8081 -> PostgreSQL :5432
                             -> habit-service    :8082 -> PostgreSQL :5432
                             -> tracking-service :8083 -> PostgreSQL :5432
                                                          -> habit-service :8082 (проверка привычки)
```

- **API Gateway** — единая точка входа: CORS, таймауты, проксирование и health-эндпоинт.
- **user-service** — управление пользователями.
- **habit-service** — управление привычками.
- **tracking-service** — отметки выполнения привычек; перед созданием отметки проверяет привычку через HTTP-вызов habit-service.
- **PostgreSQL** — общая база данных; схема создаётся из `db/init.sql`.

Каждый сервис — отдельное Java-приложение с собственным пулом соединений HikariCP и health-эндпоинтом.

Подробное описание по методологии C4 (Context, Container, Component) — в [docs/architecture/ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md).

## Технологии

| Компонент | Версия |
|-----------|--------|
| Java | 17 |
| Javalin | 6.1.3 |
| Maven | 3.9+ |
| PostgreSQL | 16 |
| HikariCP | 5.1.0 |
| Jackson | 2.16.1 |
| JUnit 5 | 5.10.2 |
| Docker Compose | v2 |

## Структура проекта

```text
habit-tracker/
├── api-gateway/            # HTTP-шлюз
├── user-service/           # сервис пользователей
├── habit-service/          # сервис привычек
├── tracking-service/       # сервис отметок выполнения
├── db/init.sql             # начальная схема БД
├── docs/architecture/      # C4-описание архитектуры
├── docker-compose.yml
├── .env.example
└── pom.xml                 # родительский POM
```

Внутри каждого сервиса код разделён на слои:

```text
src/main/java/com/habittracker/<service>/
├── <Service>Application.java  # точка входа
├── config/                    # конфигурация из переменных окружения
├── db/                        # пул соединений
├── domain/                    # модели и DTO с валидацией
├── repository/                # SQL и доступ к данным
├── error/                     # ApiException, ErrorResponse
└── web/                       # маршруты Javalin
```

## Быстрый старт

Требуется Docker Desktop с Docker Compose v2.

```bash
docker compose up -d --build
docker compose ps
```

При первом запуске PostgreSQL автоматически применит `db/init.sql`. Если база уже существовала со старой схемой, пересоздайте том:

```bash
docker compose down -v
docker compose up -d --build
```

Проверка шлюза:

```bash
curl http://localhost:8080/health
```

## API

Все запросы идут через API Gateway на `http://localhost:8080`.

| Метод | Путь | Описание | Успешный ответ |
|-------|------|----------|----------------|
| GET | `/health` | Состояние шлюза | `200 {"status":"UP"}` |
| GET | `/users` | Список пользователей | `200 [...]` |
| POST | `/users` | Создать пользователя | `201 {...}` |
| GET | `/habits` | Список привычек | `200 [...]` |
| GET | `/habits/{id}` | Привычка по id | `200 {...}` |
| POST | `/habits` | Создать привычку | `201 {...}` |
| GET | `/trackings` | Список отметок (`?habitId=` — фильтр) | `200 [...]` |
| POST | `/trackings` | Отметить выполнение привычки | `201 {...}` |

### Примеры

Создать пользователя:

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Ivan","email":"ivan@test.com"}'
```

Ответ:

```json
{"id":1,"name":"Ivan","email":"ivan@test.com"}
```

Получить пользователей:

```bash
curl http://localhost:8080/users
```

Создать привычку:

```bash
curl -X POST http://localhost:8080/habits \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"title":"Drink water"}'
```

Ответ:

```json
{"id":1,"userId":1,"title":"Drink water"}
```

Получить привычки:

```bash
curl http://localhost:8080/habits
```

Отметить выполнение привычки:

```bash
curl -X POST http://localhost:8080/trackings \
  -H "Content-Type: application/json" \
  -d '{"habitId":1,"completedOn":"2026-11-17"}'
```

Ответ:

```json
{"id":1,"habitId":1,"completedOn":"2026-11-17"}
```

Получить отметки (все или по привычке):

```bash
curl http://localhost:8080/trackings
curl "http://localhost:8080/trackings?habitId=1"
```

### Ошибки

Ошибки возвращаются в едином JSON-формате:

```json
{"error":"Field 'email' must be a valid email address"}
```

| Код | Когда возникает |
|-----|-----------------|
| 400 | Некорректный JSON, не прошла валидация, неверный формат даты |
| 404 | Маршрут или ресурс не найден |
| 409 | Пользователь с таким email уже существует; привычка уже отмечена в эту дату |
| 422 | Указанный `userId` или `habitId` не существует |
| 502 / 504 | Недоступен или не ответил внутренний сервис |

## Переменные окружения

| Переменная | Сервис | По умолчанию | Назначение |
|------------|--------|--------------|------------|
| `PORT` | все | 8080 / 8081 / 8082 / 8083 | Порт HTTP |
| `DB_URL` | user, habit, tracking | `jdbc:postgresql://localhost:5432/app` | JDBC URL |
| `DB_USER` | user, habit, tracking | `postgres` | Пользователь БД |
| `DB_PASS` | user, habit, tracking | `1234` | Пароль БД |
| `DB_POOL_SIZE` | user, habit, tracking | `10` | Размер пула HikariCP |
| `DB_CONNECTION_TIMEOUT_MS` | user, habit, tracking | `5000` | Таймаут получения соединения |
| `USER_SERVICE_URL` | gateway | `http://localhost:8081` | Адрес user-service |
| `HABIT_SERVICE_URL` | gateway, tracking | `http://localhost:8082` | Адрес habit-service |
| `TRACKING_SERVICE_URL` | gateway | `http://localhost:8083` | Адрес tracking-service |
| `HABIT_SERVICE_TIMEOUT_SECONDS` | tracking | `5` | Таймаут вызова habit-service |
| `REQUEST_TIMEOUT_SECONDS` | gateway | `10` | Таймаут запроса к сервису |
| `CORS_ALLOWED_ORIGIN` | gateway | `*` | Разрешённый origin |

Для локального запуска скопируйте `.env.example` в `.env` и измените значения.

## Схема базы данных

```sql
users     (id, name, email UNIQUE)
habits    (id, user_id -> users.id ON DELETE CASCADE, title)
trackings (id, habit_id -> habits.id ON DELETE CASCADE, completed_on DATE,
           UNIQUE (habit_id, completed_on))
```

Индексы `idx_habits_user_id` и `idx_trackings_habit_id` ускоряют выборку по пользователю и привычке.
Ограничение `UNIQUE (habit_id, completed_on)` не даёт отметить одну привычку дважды в один день.

## Разработка

Сборка всех модулей:

```bash
mvn clean package
```

Тесты:

```bash
mvn test
```

Запуск сервиса локально (пример для user-service):

```bash
mvn -pl user-service -am package
java -jar user-service/target/user-service-1.0-SNAPSHOT.jar
```

Для локального запуска нужен PostgreSQL на `localhost:5432` с базой `app` и схемой из `db/init.sql`.

## Скриншоты

- **Запуск Docker Compose:** ![Docker PS](docs/screenshots/1_docker_ps.png)
- **Проверка API:** ![API Requests](docs/screenshots/2_api_requests.png)
- **Данные в PostgreSQL:** ![Database](docs/screenshots/3_database.png)
