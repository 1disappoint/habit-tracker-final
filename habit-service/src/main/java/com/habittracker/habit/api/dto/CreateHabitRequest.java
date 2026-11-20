package com.habittracker.habit.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateHabitRequest(

        @Positive(message = "Field 'userId' must be a positive integer")
        long userId,

        @NotBlank(message = "Field 'title' is required")
        @Size(max = 255, message = "Field 'title' must not exceed 255 characters")
        String title
) {

    public CreateHabitRequest {
        title = title == null ? null : title.trim();
    }
}
