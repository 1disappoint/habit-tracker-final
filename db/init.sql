-- Начальная схема базы данных Habit Tracker.
-- Скрипт выполняется автоматически официальным образом PostgreSQL при первом старте.

CREATE TABLE IF NOT EXISTS users (
    id    SERIAL PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS habits (
    id      SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    title   VARCHAR(255) NOT NULL,
    CONSTRAINT fk_habits_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_habits_user_id ON habits (user_id);
