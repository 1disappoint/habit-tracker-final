package com.habittracker.tracking.client;

import com.habittracker.tracking.config.AppConfig;
import com.habittracker.tracking.error.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

/**
 * Проверяет существование привычки через HTTP-вызов habit-service.
 * Это пример межсервисного взаимодействия: tracking-service не лезет
 * в чужую таблицу, а спрашивает владельца данных.
 */
public final class HabitServiceClient {

    private static final Logger log = LoggerFactory.getLogger(HabitServiceClient.class);

    private final HttpClient httpClient;
    private final String habitServiceUrl;
    private final Duration timeout;

    public HabitServiceClient(AppConfig config) {
        this.habitServiceUrl = config.habitServiceUrl();
        this.timeout = config.habitServiceTimeout();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public boolean exists(int habitId) {
        String url = habitServiceUrl + "/habits/" + habitId;

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            if (response.statusCode() == 200) {
                return true;
            }
            if (response.statusCode() == 404) {
                return false;
            }
            log.warn("Unexpected response from habit-service: {} for {}", response.statusCode(), url);
            throw new ApiException(502, "Habit service returned unexpected status " + response.statusCode());
        } catch (HttpTimeoutException exception) {
            log.warn("habit-service request timed out: {}", url);
            throw new ApiException(504, "Habit service timed out");
        } catch (IOException exception) {
            log.warn("habit-service is unavailable: {} ({})", url, exception.getMessage());
            throw new ApiException(502, "Habit service is unavailable");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(502, "Request to habit service was interrupted");
        }
    }
}
