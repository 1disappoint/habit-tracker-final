package com.habittracker.tracking.domain;

import com.habittracker.tracking.error.ApiException;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

public record CreateTrackingRequest(int habitId, String completedOn) {

    /**
     * Допуск на часовые пояса: клиент может быть в UTC+14, а сервер — в UTC.
     * Поэтому «сегодня» клиента может быть на день впереди даты сервера.
     * Даты дальше этого допуска считаем ошибкой ввода.
     */
    private static final long FUTURE_DATE_TOLERANCE_DAYS = 1;

    public void validate() {
        if (habitId <= 0) {
            throw new ApiException(400, "Field 'habitId' must be a positive integer");
        }
        if (completedOn == null || completedOn.isBlank()) {
            throw new ApiException(400, "Field 'completedOn' is required (format: yyyy-MM-dd)");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(completedOn.trim());
        } catch (DateTimeParseException exception) {
            throw new ApiException(400, "Field 'completedOn' must be a valid date in format yyyy-MM-dd");
        }

        LocalDate latestAllowed = LocalDate.now(ZoneOffset.UTC).plusDays(FUTURE_DATE_TOLERANCE_DAYS);
        if (date.isAfter(latestAllowed)) {
            throw new ApiException(400, "Field 'completedOn' cannot be in the future");
        }
    }

    public LocalDate date() {
        return LocalDate.parse(completedOn.trim());
    }
}
