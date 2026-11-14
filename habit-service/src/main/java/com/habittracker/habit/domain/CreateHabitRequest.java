package com.habittracker.habit.domain;

import com.habittracker.habit.error.ApiException;

public record CreateHabitRequest(int userId, String title) {

    private static final int MAX_TITLE_LENGTH = 255;

    public CreateHabitRequest {
        title = title == null ? null : title.trim();
    }

    public void validate() {
        if (userId <= 0) {
            throw new ApiException(400, "Field 'userId' must be a positive integer");
        }
        if (title == null || title.isBlank()) {
            throw new ApiException(400, "Field 'title' is required");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new ApiException(400, "Field 'title' must not exceed " + MAX_TITLE_LENGTH + " characters");
        }
    }
}
