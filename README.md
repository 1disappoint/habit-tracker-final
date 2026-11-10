# Habit Tracker System

## Описание
Система для отслеживания привычек, состоящая из микросервисов. 
Пользователи могут создавать привычки и отмечать их выполнение.

## Архитектура
Система включает в себя:
- **API Gateway** (порт 8080) — единая точка входа.
- **User Service** (порт 8081) — управление пользователями.
- **Habit Service** (порт 8082) — управление привычками.
- **PostgreSQL** (порт 5432) — база данных.

## Технологии
- Java 17
- Javalin (веб-фреймворк)
- Maven
- PostgreSQL 16
- Docker & Docker Compose

## Запуск проекта
1. Убедитесь, что у вас установлен Docker Desktop.
2. В корневой папке проекта выполните команду:
   ```bash
   docker compose up -d --build
3. Дождитесь запуска всех контейнеров.

##Проверка работы (API)
1. Создать пользователя
- curl -X POST http://localhost:8080/users -H "Content-Type: application/json" -d "{\"name\":\"Ivan\",\"email\":\"ivan@test.com\"}"
2. Получить список пользователей
- curl http://localhost:8080/users
3. Создать привычку
- curl -X POST http://localhost:8080/habits -H "Content-Type: application/json" -d "{\"userId\":1,\"title\":\"Drink water\"}"
4. Получить список привычек
- curl http://localhost:8080/habits