package com.habittracker.habit.config;

import java.time.Duration;

public record AppConfig(
        int port,
        String dbUrl,
        String dbUser,
        String dbPassword,
        int dbPoolSize,
        Duration dbConnectionTimeout
) {

    public static AppConfig fromEnvironment() {
        return new AppConfig(
                intEnv("PORT", 8082),
                env("DB_URL", "jdbc:postgresql://localhost:5432/app"),
                env("DB_USER", "postgres"),
                env("DB_PASS", "1234"),
                intEnv("DB_POOL_SIZE", 10),
                Duration.ofMillis(intEnv("DB_CONNECTION_TIMEOUT_MS", 5000))
        );
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static int intEnv(String name, int defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Environment variable " + name + " must be an integer, but was: " + value,
                    exception
            );
        }
    }
}
