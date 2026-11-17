package com.habittracker.tracking.domain;

import com.habittracker.tracking.error.ApiException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public record CreateTrackingRequest(int habitId, String completedOn) {

    public void validate() {
        if (habitId <= 0) {
            throw new ApiException(400, "Field 'habitId' must be a positive integer");
        }
        if (completedOn == null || completedOn.isBlank()) {
            throw new ApiException(400, "Field 'completedOn' is required (format: yyyy-MM-dd)");
        }
        try {
            LocalDate.parse(completedOn.trim());
        } catch (DateTimeParseException exception) {
            throw new ApiException(400, "Field 'completedOn' must be a valid date in format yyyy-MM-dd");
        }
        if (LocalDate.parse(completedOn.trim()).isAfter(LocalDate.now())) {
            throw new ApiException(400, "Field 'completedOn' cannot be in the future");
        }
    }

    public LocalDate date() {
        return LocalDate.parse(completedOn.trim());
    }
}
