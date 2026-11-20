package com.habittracker.habit.domain.model;

/**
 * Привычка — корень агрегата habit-service.
 */
public record Habit(long id, long userId, String title) {
}
