package com.habittracker.gateway.config;

import java.time.Duration;

public record GatewayConfig(
        int port,
        String userServiceUrl,
        String habitServiceUrl,
        String corsAllowedOrigin,
        Duration requestTimeout
) {

    public static GatewayConfig fromEnvironment() {
        return new GatewayConfig(
                intEnv("PORT", 8080),
                urlEnv("USER_SERVICE_URL", "http://localhost:8081"),
                urlEnv("HABIT_SERVICE_URL", "http://localhost:8082"),
                env("CORS_ALLOWED_ORIGIN", "*"),
                Duration.ofSeconds(intEnv("REQUEST_TIMEOUT_SECONDS", 10))
        );
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static String urlEnv(String name, String defaultValue) {
        String value = env(name, defaultValue);
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
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
