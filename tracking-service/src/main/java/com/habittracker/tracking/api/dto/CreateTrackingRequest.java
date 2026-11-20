package com.habittracker.tracking.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateTrackingRequest(

        @Positive(message = "Field 'habitId' must be a positive integer")
        long habitId,

        @NotNull(message = "Field 'completedOn' is required (format: yyyy-MM-dd)")
        LocalDate completedOn
) {
}
